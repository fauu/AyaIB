package services

import java.io.File

import scala.{Option, Some}
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

import play.api.libs.concurrent.Execution.Implicits._

import com.sksamuel.scrimage.{AsyncImage, Format}
import entities._
import exceptions._
import forms.PostForm
import reactivemongo.api.gridfs.DefaultFileToSave
import reactivemongo.bson.BSONObjectID
import repositories.{BoardRepositoryComponent, FileRepositoryComponent, PostRepositoryComponent, ThreadRepositoryComponent}
import utils.Utils
import wrappers.FileWrapper

trait BoardServiceComponent {

  def boardService: BoardService

  trait BoardService {
    def listBoards: Future[List[entities.Board]]

    def findBoardByName(name: String): Future[Option[Board]]

    def addPost(boardName: String,
                threadNoOption: Option[Int] = None,
                postData: PostForm,
                fileWrapperOption: Option[FileWrapper]): Future[Try[Int]]

    def findBoardLastPostNo(name: String): Future[Option[Int]]

    def findBoardWithSingleThread(boardName: String, threadNo: Int): Future[Try[(Board, Thread)]]

  }

}

trait BoardServiceComponentImpl extends BoardServiceComponent {
  this: BoardRepositoryComponent
        with ThreadRepositoryComponent
        with PostRepositoryComponent
        with FileRepositoryComponent =>

  def boardService = new BoardServiceImpl

  class BoardServiceImpl extends BoardService {

    def listBoards = boardRepository.findAllSimple

    def findBoardByName(name: String) = boardRepository.findByName(name)

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
            Some(board) <- boardRepository.findByNameSimple(boardName)

            _ <- fileValidity(board.config, fileWrapper)

            (mainWrapper: FileWrapper, thumbWrapper: FileWrapper, fileMetadata: FileMetadata)
              <- processFile(fileWrapper)

            timestamp = System.currentTimeMillis()

            thumbFileToSave = DefaultFileToSave(filename = generateFilename(fileWrapper, timestamp, thumbnail = true),
                                                contentType = thumbWrapper.contentType)

            mainFileToSave = DefaultFileToSave(filename = generateFilename(fileWrapper, timestamp),
                                               contentType = fileWrapper.contentType)

            _ <- fileRepository saveThumbnail (thumbWrapper.file, thumbFileToSave)

            _ <- fileRepository save (fileWrapper.file, mainFileToSave)
          } yield (Some(mainFileToSave.filename), Some(fileMetadata), Some(thumbFileToSave.filename))
        case _ => Future.successful((None, None, None))
      }

      (for {
        lastPostNoOption <- findBoardLastPostNo(boardName)

        _ <- boardRepository.incrementLastPostNo(boardName)

        Some(lastPostNo) <- findBoardLastPostNo(boardName)

        (fileNameOption: Option[String], fileMetadataOption: Option[FileMetadata], thumbnailNameOption: Option[String])
          <- futureFileInfo

        newPost <- Future.successful {
          Post(no = lastPostNo,
               content = postData.content,
               fileName = fileNameOption,
               fileMetadata = fileMetadataOption,
               thumbnailName = thumbnailNameOption)
        }

        _ <- threadNoOption match {
          case Some(threadNo) => postRepository.add(boardName, threadNo, newPost)
          case _ => threadRepository.add(boardName, Thread(_id = Some(BSONObjectID.generate), op = newPost))
        }
      } yield {
        Success(newPost.no)
      }) recover {
        case (ex: IncorrectInputException) => Failure(ex)
        case (ex: Exception) => Failure(new PersistenceException(s"Cannot save thread to the database: $ex"))
      }
    }

    def findBoardLastPostNo(name: String) =
      boardRepository.findByNameSimple(name) map {
        case Some(board) => Some(board.lastPostNo)
        case _ => None
      }

    def findBoardWithSingleThread(boardName: String, threadNo: Int) = {
      (for {
        boardOption <- boardRepository.findByNameSimple(boardName)
        threadOption <- threadRepository.findByBoardNameAndNo(boardName, threadNo)
      } yield (boardOption, threadOption) match {
        case (Some(board), Some(thread)) => Success((board, thread))
        case _ => Failure(new PersistenceException())
      }) recover {
        case (ex: Exception) => Failure(new PersistenceException(s"Cannot retrieve board with single thread: $ex"))
      }
    }

  }

}
