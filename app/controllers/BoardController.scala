package controllers

import play.api.mvc.{Action, Controller}
import scala.concurrent.ExecutionContext
import ExecutionContext.Implicits.global
import entities.Board
import context.Context

object BoardController extends Controller {

  val boardService = Context.boardService

  def fixUrlAndRedirect(name: String) = Action {
    Redirect(routes.BoardController.show(name))
  }

  def show(name: String) = Action.async {
    boardService.findBoardByName(name) map {
      case Some(board) => Ok(views.html.board(board))
      case _ => NotFound(views.html.notFound())
    }
  }

}