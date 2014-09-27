package controllers

import jp.t2v.lab.play2.auth.LoginLogout
import scala.concurrent.ExecutionContext.Implicits.global

import play.api.mvc.{Action, Controller}

import auth.AuthConfigImpl
import models.forms.StaffLoginForm
import scala.concurrent.Future

import com.github.t3hnar.bcrypt._
import context.AyaIBContext

object StaffAuthController extends Controller with LoginLogout with AuthConfigImpl {

  def showLoginForm = Action { implicit request =>
    Ok(views.html.staff.login(StaffLoginForm.get))
  }

  def logout = Action.async { implicit request =>
    gotoLogoutSucceeded map (_ flashing("success" -> "Logged out"))
  }

  def authenticate = Action.async { implicit request =>
    StaffLoginForm.get.bindFromRequest fold (
      formWithErrors => { Future.successful(BadRequest(views.html.staff.login(formWithErrors))) },
      staffMemberData => {
        staffService.authenticateMember(staffMemberData.id, staffMemberData.password) flatMap {
          case Some(member) => gotoLoginSucceeded(member.id)
          case _ => Future.successful {
            Redirect(routes.StaffAuthController.showLoginForm()) flashing ("error" -> "Invalid credentials")
          }
        }
      }
    )
  }

}
