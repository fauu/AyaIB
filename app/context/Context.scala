package context

import repositories.{FileRepositoryComponentImpl, ThreadRepositoryComponentImpl, BoardRepositoryComponentImpl}
import services.{FileServiceComponentImpl, BoardServiceComponentImpl}

object Context {

  val boardServiceComponent = new BoardServiceComponentImpl with BoardRepositoryComponentImpl
                                                            with ThreadRepositoryComponentImpl
                                                            with FileRepositoryComponentImpl

  val fileServiceComponent = new FileServiceComponentImpl with FileRepositoryComponentImpl

  val boardService = boardServiceComponent.boardService

  val fileService = fileServiceComponent.fileService

}
