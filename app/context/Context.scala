package context

import repositories.BoardRepositoryComponentImpl
import services.BoardServiceComponentImpl

object Context {

  val boardServiceComponent = new BoardServiceComponentImpl with BoardRepositoryComponentImpl

  val boardService = boardServiceComponent.boardService

}
