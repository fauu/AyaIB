package services

import scala.{Option, Some}
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

import entities._
import exceptions._
import forms.PostForm
import play.api.libs.concurrent.Execution.Implicits._
import reactivemongo.api.gridfs.DefaultFileToSave
import reactivemongo.bson.BSONObjectID
import repositories.{BoardRepositoryComponent, FileRepositoryComponent, ThreadRepositoryComponent}
import utils.Utils
import wrappers.FileWrapper
import java.io.File
import com.sksamuel.scrimage.{AsyncImage, Format}

trait BoardServiceComponent {

  def boardService: BoardService

  trait BoardService {
    def listBoards: Future[List[entities.Board]]
    def findBoardByName(name: String): Future[Option[Board]]
    def addThread(boardName: String, opPostData: PostForm, fileWrapper: FileWrapper): Future[Try[Int]]
    def findBoardLastPostNo(name: String): Future[Option[Int]]
  }

}

trait BoardServiceComponentImpl extends BoardServiceComponent {
  this: BoardRepositoryComponent
        with ThreadRepositoryComponent
        with FileRepositoryComponent =>

  def boardService = new BoardServiceImpl

  class BoardServiceImpl extends BoardService {
    def listBoards = boardRepository.findAllSimple

    def findBoardByName(name: String) = boardRepository.findByName(name)

    private def thumbnailImage(image: AsyncImage): Future[AsyncImage] = {
      val thumbnailMaxDimension = 250

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
                                               contentType = "image/jpeg")
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

    def addThread(boardName: String, opPostData: PostForm, fileWrapper: FileWrapper) =
      (for {
        Some(board) <- boardRepository.findByNameSimple(boardName)

        _ <- fileValidity(board.config, fileWrapper)

        (mainWrapper: FileWrapper, thumbWrapper: FileWrapper, metadata: FileMetadata)
          <- processFile(fileWrapper)

        fileMetadata = FileMetadata.fileMetadataBSONHandler write metadata

        timestamp = System.currentTimeMillis()

        thumbFileToSave = DefaultFileToSave(filename = generateFilename(fileWrapper, timestamp, thumbnail = true),
                                            contentType = thumbWrapper.contentType)

        mainFileToSave = DefaultFileToSave(filename = generateFilename(fileWrapper, timestamp),
                                           contentType = fileWrapper.contentType,
                                           metadata = fileMetadata)

        thumbResult <- fileRepository.saveThumbnail(thumbWrapper.file, thumbFileToSave)

        fileResult <- fileRepository.save(fileWrapper.file, mainFileToSave)

        _ <- boardRepository.incrementLastPostNo(boardName)

        Some(lastPostNo) <- findBoardLastPostNo(boardName)

        newThread = Thread(_id = Some(BSONObjectID.generate),
                           op = Post(no = lastPostNo, content = opPostData.content),
                           replies = List[Post]())

        _ <- threadRepository.add(boardName, newThread)

        _ <- threadRepository.setOpFileRefs(boardName, newThread._id, fileResult.id, thumbResult.id)
      } yield {
        Success(newThread.op.no)
      }) recover {
        case (ex: IncorrectInputException) => Failure(ex)
        case (ex: Exception) => Failure {
          new PersistenceException(s"Cannot save thread to the database: $ex")
        }
      }

    def findBoardLastPostNo(name: String) =
      boardRepository.findByNameSimple(name) map {
        case Some(board) => Some(board.lastPostNo)
        case _ => None
      }
  }

}
