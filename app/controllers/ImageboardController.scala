package controllers

import play.api.mvc.{Action, Controller}
import scala.concurrent.ExecutionContext
import ExecutionContext.Implicits.global
import entities.Board
import context.Context

object ImageboardController extends Controller {

  val boardService = Context.boardService

  def index = Action.async {
    boardService.listBoards map {
      case boards: List[Board] => Ok(views.html.index(boards))
      case _ => NotFound(views.html.notFound())
    }
  }

}