package controllers

import play.api.mvc.{Action, Controller}

import scala.concurrent.ExecutionContext.Implicits.global

import context.Context
import entities.Board

object ImageboardController extends Controller {

  val boardService = Context.boardService

  def index = Action.async {
    boardService.listBoards map {
      case boards: List[Board] => Ok(views.html.index(boards))
      case _ => NotFound(views.html.notFound())
    }
  }

}