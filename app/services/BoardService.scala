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

    private def processFile(fileWrapper: FileWrapper): (FileWrapper, FileWrapper, FileMetadata) = {
      (fileWrapper, fileWrapper, FileMetadata(originalName = "testOrigName", dimensions = "testOrigDims"))
    }

    def addThread(boardName: String, opPostData: PostForm, fileWrapper: FileWrapper) = {
      boardRepository.findByNameSimple(boardName) map { boardOption =>
        boardOption map { board =>
          if (board.config.allowedContentTypes contains fileWrapper.contentType.getOrElse("")) {
            processFile(fileWrapper) match {
              case (mainWrapper: FileWrapper, thumbWrapper: FileWrapper, metadata: FileMetadata) =>
                val fileMetadata = FileMetadata.fileMetadataBSONHandler write metadata

                val thumbFileToSave = DefaultFileToSave(filename = "TestThumbName",
                  contentType = thumbWrapper.contentType)
                val mainFileToSave = DefaultFileToSave(filename = "TestFilename",
                  contentType = fileWrapper.contentType,
                  metadata = fileMetadata)

                val futureThumbResult = fileRepository.saveThumbnail(file = thumbWrapper.file,
                  fileToSave = thumbFileToSave)
                val futureFileResult = fileRepository.save(file = fileWrapper.file, fileToSave = mainFileToSave)

                futureThumbResult flatMap { thumbResult =>
                  futureFileResult flatMap { fileResult =>
                    boardRepository.incrementLastPostNo(boardName) flatMap { lastError =>
                      findBoardLastPostNo(boardName) map {
                        case Some(lastPostNo) =>
                          val newThread = Thread(_id = Some(BSONObjectID.generate),
                            op = Post(no = lastPostNo, content = opPostData.content),
                            replies = List[Post]())

                          threadRepository.add(boardName, newThread) flatMap { lastError =>
                            threadRepository.setOpFileRefs(boardName, newThread._id, fileResult.id, thumbResult.id) map {
                              lastError => Success(newThread.op.no)
                            }
                          }
                        case _ => Future.successful(Failure(new PersistenceException(s"Cannot retrieve last post number for board $boardName")))
                      } flatMap (x => x)
                    }
                  }
                } recover { case _ => Failure(new PersistenceException("Cannot save file to the database")) }
            }
          } else Future.successful(Failure(new BadInputException("Corrupted file or forbidden file type")))
        } getOrElse Future.successful(Failure(new PersistenceException(s"Cannot retrieve board data for board $boardName")))
      } flatMap (x => x)
    }

    def findBoardLastPostNo(name: String) =
      boardRepository.findByNameSimple(name) map {
        case Some(board) => Some(board.lastPostNo)
        case _ => None
      }
  }

}
