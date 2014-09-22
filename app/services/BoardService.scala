package services

import java.io.File

import scala.{Option, Some}
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

import play.api.libs.concurrent.Execution.Implicits._

import com.sksamuel.scrimage.{AsyncImage, Format}
import models.entities._
import utils.exceptions._
import models.forms.PostForm
import reactivemongo.api.gridfs.DefaultFileToSave
import reactivemongo.bson.BSONObjectID
import repositories.{BoardRepositoryComponent, FileRepositoryComponent, ThreadRepositoryComponent}
import utils.Utils
import models.wrappers.FileWrapper
import com.github.nscala_time.time.Imports.DateTime

trait BoardServiceComponent {

  def boardService: BoardService

  trait BoardService {

    def addPost(boardName: String,
                threadNoOption: Option[Int] = None,
                postData: PostForm,
                fileWrapperOption: Option[FileWrapper]): Future[Try[Int]]

    def findAllBoards: Future[List[models.entities.Board]]

    def findBoard(name: String): Future[Option[Board]]

    def findBoardLastPostNo(name: String): Future[Option[Int]]

    def findBoardWithThread(boardName: String, threadNo: Int): Future[Try[(Board, Thread)]]

    def findBoardWithAllThreads(boardName: String): Future[Try[(Board, List[Thread])]]

  }

}

trait BoardServiceComponentImpl extends BoardServiceComponent {
  this: BoardRepositoryComponent
        with ThreadRepositoryComponent
        with FileRepositoryComponent =>

  def boardService = new BoardServiceImpl

  class BoardServiceImpl extends BoardService {

    private def thumbnailImage(image: AsyncImage): Future[AsyncImage] = {
      val thumbnailMaxDimension = 200

      if (image.dimensions._1 > thumbnailMaxDimension || image.dimensions._2 > thumbnailMaxDimension) {
        if (image.ratio >= 1) image.scaleToWidth(thumbnailMaxDimension)
        else image.scaleToHeight(thumbnailMaxDimension)
      } else Future(image)
    }

    // TODO: Add indication text to mark gif thumbnails
    // TODO: Make animated gif thumbnails
    private def processFile(fileWrapper: FileWrapper): Future[(FileWrapper, FileWrapper, FileMetadata)] = {
      fileWrapper.contentType getOrElse "" match {
        case contentType @ ("image/jpeg" | "image/png" | "image/gif") =>
          for {
            image <- AsyncImage(fileWrapper.file)

            imageCopy <- AsyncImage(fileWrapper.file)

            thumbnail <- thumbnailImage(imageCopy)

            // TODO: Stream this instead of using temp file?
            thumbnailFile = File.createTempFile("tmp", "ayafile")

            _ <- thumbnail.writer(Format.JPEG) write thumbnailFile

            thumbnailWrapper = new FileWrapper(file = thumbnailFile,
                                               filename = fileWrapper.filename,
                                               contentType = Some("image/jpeg"))
          } yield {
            (fileWrapper,
             thumbnailWrapper,
             FileMetadata(originalName = fileWrapper.filename,
                          dimensions = image.dimensions.productIterator map { _.toString } mkString "x",
                          size = Utils.humanizeFileLength(fileWrapper.file.length)))
          }
        case _ => throw new UnsupportedOperationException("File format not supported")
      }
    }

    private def fileValidity(boardConfig: BoardConfig, fileWrapper: FileWrapper): Future[Unit] =
      if (boardConfig.allowedContentTypes contains fileWrapper.contentType.getOrElse("")) Future.successful(Unit)
      else Future.failed(new IncorrectInputException("Corrupted file or forbidden file type"))

    private def generateFilename(fileWrapper: FileWrapper, timestamp: Long, thumbnail: Boolean = false): String = {
      val extension = Utils.contentTypeToExtension(fileWrapper.contentType.get).getOrElse("ayafile")
      val name = if (thumbnail) timestamp.toString + "_thumb" else timestamp.toString
      name + "." + extension
    }

    def addPost(
      boardName: String,
      threadNoOption: Option[Int] = None,
      postData: PostForm,
      fileWrapperOption: Option[FileWrapper]
    ) = {
      val futureFileInfo = fileWrapperOption match {
        case Some(fileWrapper) =>
          for {
            Some(board) <- boardRepository findOneByName boardName // FIXME: DRY

            _ <- fileValidity(board.config, fileWrapper)

            (mainWrapper: FileWrapper, thumbWrapper: FileWrapper, fileMetadata: FileMetadata)
              <- processFile(fileWrapper)

            timestamp = System.currentTimeMillis()

            thumbFileToSave = DefaultFileToSave(filename = generateFilename(fileWrapper, timestamp, thumbnail = true),
                                                contentType = thumbWrapper.contentType)

            mainFileToSave = DefaultFileToSave(filename = generateFilename(fileWrapper, timestamp),
                                               contentType = fileWrapper.contentType)

            _ <- fileRepository add (thumbWrapper.file, thumbFileToSave, thumbnail = true)

            _ <- fileRepository add (fileWrapper.file, mainFileToSave)
          } yield (Some(mainFileToSave.filename), Some(fileMetadata), Some(thumbFileToSave.filename))
        case _ => Future.successful((None, None, None))
      }

      (for {
        Some(board) <- boardRepository findOneByName boardName // FIXME: DRY

        _ <- boardRepository.incrementLastPostNo(boardName)

        (fileNameOption: Option[String], fileMetadataOption: Option[FileMetadata], thumbnailNameOption: Option[String])
          <- futureFileInfo

        newPost <- Future.successful {
          Post(no = board.lastPostNo + 1,
               subject = postData.subject,
               email = postData.email,
               content = postData.content,
               date = DateTime.now,
               fileName = fileNameOption,
               fileMetadata = fileMetadataOption,
               thumbnailName = thumbnailNameOption)
        }

        _ <- threadNoOption match {
          case Some(threadNo) =>
             threadRepository.findOneByBoardAndNo(board, threadNo) flatMap { // TODO: This is excessive, fix
               case Some(thread) =>
                 threadRepository.addReply(board, thread, newPost) flatMap { lastError =>
                   if (newPost.email.getOrElse("") != "sage")
                     threadRepository.updateBumpDate(board, threadNo, newPost.date)
                   else
                     Future.successful(lastError)
                 }
               case _ => Future.failed(new PersistenceException("Cannot add reply: cannot retrieve thread"))
            }
          case _ => threadRepository.add(board, Thread(bumpDate = newPost.date, op = newPost))
        }
      } yield {
        Success(newPost.no)
      }) recover {
        case (ex: IncorrectInputException) => Failure(ex)
        case (ex: Exception) => Failure(new PersistenceException(s"Cannot save thread: $ex"))
      }
    }

    def findAllBoards = boardRepository.findAll

    def findBoard(name: String) = boardRepository.findOneByName(name)

    def findBoardLastPostNo(name: String) =
      boardRepository.findOneByName(name) map {
        case Some(board) => Some(board.lastPostNo)
        case _ => None
      }

    def findBoardWithThread(boardName: String, threadNo: Int) = {
      boardRepository.findOneByName(boardName) flatMap {
        case Some(board) =>
          threadRepository.findOneByBoardAndNo(board, threadNo) map {
            case Some(thread) => Success((board, thread))
            case _ => Failure(new PersistenceException("Cannot retrieve thread"))
          }
        case _ => Future.successful(Failure(new PersistenceException("Cannot retrieve board")))
      }
    }

    def findBoardWithAllThreads(boardName: String) = {
      boardRepository.findOneByName(boardName) flatMap {
        case Some(board) =>
          threadRepository.findByBoard(board) map { threads =>
            Success((board, threads))
          }
        case _ => Future.successful(Failure(new PersistenceException("Cannot retrieve board")))
      }
    }

  }

}
