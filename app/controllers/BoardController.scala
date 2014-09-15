package controllers

import scala.concurrent.{ExecutionContext, Future}

import ExecutionContext.Implicits.global
import context.Context
import entities.{FileMetadata, Post, Thread}
import forms.PostForm
import play.api.mvc.{Action, Controller}
import reactivemongo.api.gridfs.DefaultFileToSave

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

  // TODO: Handle errors
  def createThread(boardName: String) = Action.async(parse.multipartFormData) { implicit request =>
    val postData: Option[PostForm] = PostForm.get.bindFromRequest.fold(
      formWithErrors => None,
      postData => Some(postData)
    )

    request.body.file("file") map { file =>
      postData map { postData =>
        boardService.findBoardLastPostNo(boardName) map {
          case Some(no) =>
            boardService.addThread(
              boardName,
              Thread(op = Post(no = no + 1, content = postData.content),
                replies = List[Post]()),
              file.ref.file,
              file.filename,
              file.contentType
            )
            Redirect(routes.BoardController.show(boardName))
          case _ => InternalServerError
        }
      } getOrElse Future(BadRequest("Form binding error."))
    } getOrElse Future(BadRequest("No file."))
  }

}