package controllers

import scala.concurrent.{ExecutionContext, Future}

import ExecutionContext.Implicits.global
import context.Context
import entities.{FileMetadata, Post, Thread}
import forms.PostForm
import play.api.mvc.{Results, Action, Controller}
import reactivemongo.api.gridfs.DefaultFileToSave
import wrappers.FileWrapper
import reactivemongo.bson.BSONObjectID
import scala.util.Success

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

  def createThread(boardName: String) = Action.async(parse.multipartFormData) { implicit request =>
    val postData = PostForm.get.bindFromRequest.fold(
      formWithErrors => None,
      postData => Some(postData)
    )

    request.body.file("file") map { file =>
      postData map { postData =>
        val futureNewThreadNoOption
            = boardService.addThread(boardName,
                                     opPostData = postData,
                                     new FileWrapper(file.ref.file, file.filename, file.contentType))

        futureNewThreadNoOption map {
          case Some(newThreadNo) => Redirect(routes.BoardController.show(boardName))
          case _ => InternalServerError
        }
      } getOrElse Future(BadRequest("Form binding error"))
    } getOrElse Future(BadRequest("No file"))
  }

}