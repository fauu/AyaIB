package controllers

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

import play.api.Logger
import play.api.libs.Files.TemporaryFile
import play.api.mvc.{Action, Controller}
import play.api.mvc.MultipartFormData.FilePart

import ExecutionContext.Implicits.global
import context.Context
import exceptions.IncorrectInputException
import forms.PostForm
import wrappers.FileWrapper

object BoardController extends Controller {

  val boardService = Context.boardService

  def fixUrlAndRedirect(name: String) = Action {
    Redirect(routes.BoardController.show(name))
  }

  def show(name: String) = Action.async { implicit request =>
    boardService.findBoardByName(name) map {
      case Some(board) => Ok(views.html.board(board = board, postForm = PostForm.get))
      case _ => NotFound(views.html.notFound())
    }
  }

  def showThread(boardName: String, no: Int) = Action.async { implicit request =>
    boardService.findBoardWithSingleThread(boardName = boardName, threadNo = no) map {
      case Success((board, thread)) => Ok(views.html.board(board, Some(thread), PostForm.get))
      case Failure(ex) =>  NotFound(views.html.notFound())
    }
  }

  def createThread(boardName: String) = Action.async(parse.multipartFormData) { implicit request =>
    val postDataOption = PostForm.get.bindFromRequest fold (formWithErrors => None, postData => Some(postData))

    request.body.file("file") map { file =>
      postDataOption map { postData =>
        val futureNewThreadNoOption
            = boardService.addPost(boardName = boardName,
                                   postData = postData,
                                   fileWrapperOption = Some(new FileWrapper(file.ref.file,
                                                                            file.filename,
                                                                            file.contentType)))

        futureNewThreadNoOption map {
          case Success(newThreadNo) => Redirect(routes.BoardController.showThread(boardName, newThreadNo))

          case Failure(ex: IncorrectInputException)
            => Redirect(routes.BoardController.show(boardName)).flashing("error" -> ex.getMessage)

          case Failure(ex) => {
            Logger.error(s"Cannot add new thread: $ex")
            Redirect(routes.BoardController.show(boardName)).flashing("failure" -> "")
          }
        }
      } getOrElse Future.successful {
        Redirect(routes.BoardController.show(boardName)).flashing("error" -> "Please fill all required fields")
      }
    } getOrElse Future.successful {
      Redirect(routes.BoardController.show(boardName)).flashing("error" -> "Please pick a file")
    }
  }

  def postInThread(boardName: String, threadNo: Int) = Action.async(parse.multipartFormData) { implicit request =>
    val postData = PostForm.get.bindFromRequest fold (formWithErrors => None, postData => Some(postData))

    postData map { postData =>
      val fileWrapperOption: Option[FileWrapper] = request.body.file("file") match {
        case Some(file: FilePart[TemporaryFile]) => Some(new FileWrapper(file.ref.file, file.filename, file.contentType))
        case _ => None
      }

      val futureNewPostNoOption = boardService.addPost(boardName, Some(threadNo), postData, fileWrapperOption)

      futureNewPostNoOption map {
        case Success(newPostNo) => Redirect(routes.BoardController.showThread(boardName, threadNo) + "#post-" + newPostNo)

        case Failure(ex: IncorrectInputException)
          => Redirect(routes.BoardController.show(boardName)).flashing("error" -> ex.getMessage)

        case Failure(ex) => {
          Logger.error(s"Cannot add new post: $ex")
          Redirect(routes.BoardController.show(boardName)).flashing("failure" -> "")
        }
      }
    } getOrElse Future.successful {
      Redirect(routes.BoardController.show(boardName)).flashing("error" -> "Please fill all required fields")
    }
  }

}