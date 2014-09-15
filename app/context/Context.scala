package context

import repositories.{ThreadRepositoryComponentImpl, BoardRepositoryComponentImpl}
import services.BoardServiceComponentImpl

object Context {

  val boardServiceComponent = new BoardServiceComponentImpl with BoardRepositoryComponentImpl
                                                            with ThreadRepositoryComponentImpl

  val boardService = boardServiceComponent.boardService

}
