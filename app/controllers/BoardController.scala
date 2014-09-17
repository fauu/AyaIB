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
import scala.util.{Failure, Success}
import exceptions.BadInputException
import play.api.Logger

object BoardController extends Controller {

  val boardService = Context.boardService

  def fixUrlAndRedirect(name: String) = Action {
    Redirect(routes.BoardController.show(name))
  }

  def show(name: String) = Action.async { implicit request =>
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
          case Success(newThreadNo) => Redirect(routes.BoardController.show(boardName))

          case Failure(ex: BadInputException)
            => Redirect(routes.BoardController.show(boardName)).flashing("error" -> ex.getMessage)

          case Failure(ex) => {
            Logger.debug(s"Cannot add new thread: ${ex.getMessage}")
            Redirect(routes.BoardController.show(boardName)).flashing("failure" -> "")
          }
        }
      } getOrElse Future.successful(BadRequest("Form binding error"))
    } getOrElse Future.successful(BadRequest("No file"))
  }

}