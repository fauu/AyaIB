package controllers

import play.api.mvc.{Action, Controller}

import scala.concurrent.ExecutionContext.Implicits.global

import context.AyaIBContext
import models.entities.Board

object ImageboardController extends Controller {

  val boardService = AyaIBContext.boardService

  def index = Action.async {
    boardService.findAllBoards map {
      case boards: List[Board] => Ok(views.html.index(boards))
      case _ => NotFound(views.html.notFound())
    }
  }

}