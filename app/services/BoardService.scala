package services

import scala.concurrent.Future
import repositories.{FileRepositoryComponent, ThreadRepositoryComponent, BoardRepositoryComponent}
import entities.{FileMetadata, Board, Thread}
import play.api.libs.concurrent.Execution.Implicits._
import reactivemongo.core.commands.LastError
import java.io.File
import reactivemongo.api.gridfs.{DefaultFileToSave, ReadFile, FileToSave}
import reactivemongo.bson.{BSONDocument, BSONValue, BSONObjectID}
import wrappers.FileWrapper
import exceptions.ServiceException

trait BoardServiceComponent {

  def boardService: BoardService

  trait BoardService {
    def listBoards: Future[List[entities.Board]]
    def findBoardByName(name: String): Future[Option[Board]]
    // Perhaps handle the mongo error in the repository and throw something more general here instead
    def addThread(boardName: String, thread: Thread, fileWrapper: FileWrapper)
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

    def addThread(boardName: String, thread: Thread, fileWrapper: FileWrapper) = {
      implicit val bsonDocumentHandler = FileMetadata.fileMetadataBSONHandler

      val futureFile = fileRepository.save(fileWrapper.file,
                                           DefaultFileToSave(filename = fileWrapper.filename,
                                                             contentType = fileWrapper.contentType,
                                                             metadata = BSONDocument("" -> FileMetadata(originalName = fileWrapper.filename,
                                                                                         dimensions = "!Not Implemented"))))
      futureFile.map { file =>
        boardRepository.incrementLastPostNo(boardName) map { lastError =>
          if (lastError.ok) threadRepository.add(boardName, thread) map { lastError =>
            if (lastError.ok) threadRepository.setOpFileRef(boardName, thread._id, file.id) map { lastError =>
              if (!lastError.ok) throw new ServiceException(lastError.errMsg.getOrElse(""))
            } else throw new ServiceException(lastError.errMsg.getOrElse(""))
          } else throw new ServiceException(lastError.errMsg.getOrElse(""))
        }
      } recover {
        case e: Throwable => throw e
      }
    }

    def findBoardLastPostNo(name: String) =
      boardRepository.findByNameSimple(name) map {
        case Some(board) => Some(board.lastPostNo)
        case _ => None
      }
  }

}
