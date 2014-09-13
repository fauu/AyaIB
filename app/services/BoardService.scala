package services

import scala.concurrent.Future
import repositories.BoardRepositoryComponent
import entities.Board

trait BoardServiceComponent {

  def boardService: BoardService

  trait BoardService {
    def listBoards: Future[List[entities.Board]]
    def findBoardByName(name: String): Future[Option[Board]]
  }

}

trait BoardServiceComponentImpl extends BoardServiceComponent {
  this: BoardRepositoryComponent =>

  def boardService = new BoardServiceImpl

  class BoardServiceImpl extends BoardService {
    def listBoards = boardRepository.findAllSimple
    def findBoardByName(name: String) = boardRepository.findByName(name)
  }

}
