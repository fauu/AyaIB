package context

import repositories._
import services._

object Context {

  val boardServiceComponent = new BoardServiceComponentImpl with BoardRepositoryComponentImpl
                                                            with ThreadRepositoryComponentImpl
                                                            with PostRepositoryComponentImpl
                                                            with FileRepositoryComponentImpl

  val boardService = boardServiceComponent.boardService

  val fileServiceComponent = new FileServiceComponentImpl with FileRepositoryComponentImpl

  val fileService = fileServiceComponent.fileService

}
