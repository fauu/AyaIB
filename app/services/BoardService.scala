package services

import scala.concurrent.Future
import repositories.{FileRepositoryComponent, ThreadRepositoryComponent, BoardRepositoryComponent}
import entities._
import play.api.libs.concurrent.Execution.Implicits._
import reactivemongo.core.commands.LastError
import java.io.File
import reactivemongo.api.gridfs.{DefaultFileToSave, ReadFile, FileToSave}
import reactivemongo.bson.{BSONDocument, BSONValue, BSONObjectID}
import wrappers.FileWrapper
import scala.Option
import scala.util.{Failure, Try, Success}
import forms.PostForm
import scala.Some
import reactivemongo.api.gridfs.DefaultFileToSave
import exceptions._
import reactivemongo.core.errors.DatabaseException

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

    private def fileValidity(boardConfig: BoardConfig, fileWrapper: FileWrapper): Future[Unit] =
      if (boardConfig.allowedContentTypes contains fileWrapper.contentType.getOrElse("")) Future.successful()
      else Future.failed(new IncorrectInputException("Corrupted file or forbidden file type"))

    private def processFile(fileWrapper: FileWrapper): (FileWrapper, FileWrapper, FileMetadata) = {
      (fileWrapper, fileWrapper, FileMetadata(originalName = "testOrigName", dimensions = "testOrigDims"))
    }

    def addThread(boardName: String, opPostData: PostForm, fileWrapper: FileWrapper) =
      (for {
        Some(board) <- boardRepository.findByNameSimple(boardName)

        _ <- fileValidity(board.config, fileWrapper)

        (mainWrapper: FileWrapper, thumbWrapper: FileWrapper, metadata: FileMetadata)
          <- Future(processFile(fileWrapper))

        fileMetadata = FileMetadata.fileMetadataBSONHandler write metadata

        thumbFileToSave = DefaultFileToSave(filename = "TestThumbName", contentType = thumbWrapper.contentType)

        mainFileToSave = DefaultFileToSave(filename = "TestFilename",
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
        case _ => Failure(new PersistenceException("Cannot save thread to the database"))
      }

    def findBoardLastPostNo(name: String) =
      boardRepository.findByNameSimple(name) map {
        case Some(board) => Some(board.lastPostNo)
        case _ => None
      }
  }

}
