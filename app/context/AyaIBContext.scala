package context

import repositories._
import services._

object AyaIBContext {

  val boardServiceComponent = new BoardServiceComponentImpl with BoardRepositoryComponentImpl
                                                            with ThreadRepositoryComponentImpl
                                                            with PostIdRepositoryComponentImpl
                                                            with QuotationRepositoryComponentImpl
                                                            with FileRepositoryComponentImpl

  val boardService = boardServiceComponent.boardService

  val fileServiceComponent = new FileServiceComponentImpl with FileRepositoryComponentImpl

  val fileService = fileServiceComponent.fileService

}
