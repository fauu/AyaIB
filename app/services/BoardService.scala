package services

import scala.concurrent.Future
import repositories.{ThreadRepositoryComponent, BoardRepositoryComponent}
import entities.{Board, Thread}
import play.api.libs.concurrent.Execution.Implicits._
import reactivemongo.core.commands.LastError

trait BoardServiceComponent {

  def boardService: BoardService

  trait BoardService {
    def listBoards: Future[List[entities.Board]]
    def findBoardByName(name: String): Future[Option[Board]]
    // Perhaps handle the mongo error in the repository and throw something more general here instead
    def addThread(boardName: String, thread: Thread): Future[LastError]
    def findBoardLastPostNo(name: String): Future[Option[Int]]
  }

}

trait BoardServiceComponentImpl extends BoardServiceComponent {
  this: BoardRepositoryComponent
        with ThreadRepositoryComponent =>

  def boardService = new BoardServiceImpl

  class BoardServiceImpl extends BoardService {
    def listBoards = boardRepository.findAllSimple

    def findBoardByName(name: String) = boardRepository.findByName(name)

    def addThread(boardName: String, thread: Thread) = {
      boardRepository.incrementLastPostNo(boardName)
      threadRepository.add(boardName, thread)
    }

    def findBoardLastPostNo(name: String) =
      boardRepository.findByNameSimple(name) map {
        case Some(board) => Some(board.lastPostNo)
        case _ => None
      }
  }

}
