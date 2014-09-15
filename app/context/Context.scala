package context

import repositories.{FileRepositoryComponentImpl, ThreadRepositoryComponentImpl, BoardRepositoryComponentImpl}
import services.BoardServiceComponentImpl

object Context {

  val boardServiceComponent = new BoardServiceComponentImpl with BoardRepositoryComponentImpl
                                                            with ThreadRepositoryComponentImpl
                                                            with FileRepositoryComponentImpl

  val boardService = boardServiceComponent.boardService

}
