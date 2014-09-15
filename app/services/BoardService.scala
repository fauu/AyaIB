package services

import scala.concurrent.Future
import repositories.{FileRepositoryComponent, ThreadRepositoryComponent, BoardRepositoryComponent}
import entities.{FileMetadata, Board, Thread}
import play.api.libs.concurrent.Execution.Implicits._
import reactivemongo.core.commands.LastError
import java.io.File
import reactivemongo.api.gridfs.{DefaultFileToSave, ReadFile, FileToSave}
import reactivemongo.bson.{BSONValue, BSONObjectID}

trait BoardServiceComponent {

  def boardService: BoardService

  trait BoardService {
    def listBoards: Future[List[entities.Board]]
    def findBoardByName(name: String): Future[Option[Board]]
    // Perhaps handle the mongo error in the repository and throw something more general here instead
    def addThread(boardName: String, thread: Thread, file: File,
                  filename: String, contentType: String): Unit
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

    def addThread(boardName: String, thread: Thread, file: File, filename: String, contentType: String) = {
      val futureFile = fileRepository.save(file, DefaultFileToSave(filename = file.filename,
                                                                   contentType = file.contentType,
                                                                   metadata = FileMetadata(originalName = file.filename,
                                                                                           dimensions = ???)))
      futureFile.map { file =>
        val incrementLastPostNoResult = boardRepository.incrementLastPostNo(boardName)
        val addThreadResult = threadRepository.add(boardName, thread)

        Future.sequence(List(incrementLastPostNoResult, addThreadResult)) onFailure {
          case e: Throwable => throw e
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
