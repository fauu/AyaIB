package context

import repositories.{PostRepositoryComponentImpl, FileRepositoryComponentImpl, ThreadRepositoryComponentImpl, BoardRepositoryComponentImpl}
import services.{FileServiceComponentImpl, BoardServiceComponentImpl}

object Context {

  val boardServiceComponent = new BoardServiceComponentImpl with BoardRepositoryComponentImpl
                                                            with ThreadRepositoryComponentImpl
                                                            with PostRepositoryComponentImpl
                                                            with FileRepositoryComponentImpl

  val fileServiceComponent = new FileServiceComponentImpl with FileRepositoryComponentImpl

  val boardService = boardServiceComponent.boardService

  val fileService = fileServiceComponent.fileService

}
