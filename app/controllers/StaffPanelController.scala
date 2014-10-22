/*
 * Copyright (C) 2014 AyaIB Developers (http://github.com/fauu/AyaIB)
 *
 * This software is licensed under the GNU General Public License
 * (version 3 or later). See the COPYING file in this distribution.
 *
 * You should have received a copy of the GNU Library General Public License
 * along with this software. If not, see <http://www.gnu.org/licenses/>.
 *
 * Authored by: Piotr Grabowski <fau999@gmail.com>
 */

package controllers

import play.api.mvc.{Action, Controller}
import scala.concurrent.ExecutionContext.Implicits.global
import auth.{Janitor, AuthConfigImpl}
import jp.t2v.lab.play2.auth.AuthElement
import context.AyaIBContext
import scala.concurrent.Future
import models.forms.BoardForm
import scala.util.{Success, Failure}
import utils.exceptions.IncorrectInputException
import play.api.Logger

object StaffPanelController extends Controller with AuthElement with AuthConfigImpl {

  val boardService = AyaIBContext.boardService

//  def index = StackAction(AuthorityKey -> Janitor) { implicit request =>
  def index = Action { implicit request =>
//    val user = loggedIn
    Ok(views.html.staff.index())
  }

  def manageBoards = Action.async { implicit request =>
    boardService.findAllBoards map (boards => Ok(views.html.staff.manageBoards(boards)))
  }

  def newBoardForm = Action { implicit request =>
    Ok(views.html.staff.addNewBoard(BoardForm.get fill BoardForm()))
  }

  def addNewBoard = Action.async { implicit request =>
    val boardForm = BoardForm.get.bindFromRequest

    val boardData = boardForm fold (formWithErrors => None, boardData => Some(boardData))

    boardData map { boardData =>
      boardService.addBoard(boardData) map {
        case Success(()) =>
          Redirect(routes.StaffPanelController.newBoardForm)
        case Failure(ex: IncorrectInputException) =>
          Redirect(routes.StaffPanelController.newBoardForm) flashing ("error" -> ex.getMessage)
        case Failure(ex) =>
          Logger error s"Cannot add new board: $ex"
          Redirect(routes.StaffPanelController.newBoardForm) flashing ("failure" -> "")
      }
    } getOrElse Future.successful(Redirect(routes.StaffPanelController.newBoardForm))
//    val postData = PostForm.get.bindFromRequest fold (formWithErrors => None, postData => Some(postData))
//
//    postData map { postData =>
//      val fileWrapperOption: Option[FileWrapper] = request.body.file("file") match {
//        case Some(file: FilePart[TemporaryFile]) =>
//          Some(new FileWrapper(file.ref.file, file.filename, file.contentType))
//        case _ => None
//      }
//
//      val futureNewPostNoOption = boardService.addPost(boardName, Some(threadNo), postData, fileWrapperOption)
//
//      futureNewPostNoOption map {
//        case Success(newPostNo) =>
//          Redirect(routes.BoardController.showThread(boardName, threadNo) + "#post-" + newPostNo)
//
//        case Failure(ex: IncorrectInputException) =>
//          Redirect(routes.BoardController.show(boardName)).flashing("error" -> ex.getMessage)
//
//        case Failure(ex) =>
//          Logger.error(s"Cannot add new post: $ex")
//          Redirect(routes.BoardController.show(boardName)).flashing("failure" -> "")
//      }
//    } getOrElse Future.successful {
//      Redirect(routes.BoardController.show(boardName)).flashing("error" -> "Please fill all required fields")
//    }
  }

}
