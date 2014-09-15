package controllers

import play.api.mvc.{Action, Controller}
import scala.concurrent.{Future, ExecutionContext}
import ExecutionContext.Implicits.global
import entities.{Post, Board, Thread}
import context.Context
import forms.PostForm
import play.api.data.Form

object BoardController extends Controller {

  val boardService = Context.boardService

  def fixUrlAndRedirect(name: String) = Action {
    Redirect(routes.BoardController.show(name))
  }

  def show(name: String) = Action.async {
    boardService.findBoardByName(name) map {
      case Some(board) => Ok(views.html.board(board, PostForm.get))
      case _ => NotFound(views.html.notFound())
    }
  }

  def createThread(boardName: String) = Action.async { implicit request =>
    PostForm.get.bindFromRequest.fold(
      formWithErrors => { Future(BadRequest) },
      postData => {
        boardService.findBoardLastPostNo(boardName) map {
          // TODO: Handle errors
          case Some(no) =>
            boardService.addThread(
              boardName,
              Thread(op = Post(no = no + 1, content = postData.content),
                     replies = List[Post]())
            )
            Redirect(routes.BoardController.show(boardName))
          case _ => InternalServerError
        }
      }
    )
  }

}