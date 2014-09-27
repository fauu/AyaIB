package controllers

import play.api.mvc.{Action, Controller}
import auth.{Janitor, AuthConfigImpl}
import jp.t2v.lab.play2.auth.AuthElement

object StaffPanelController extends Controller with AuthElement with AuthConfigImpl {

//  def index = StackAction(AuthorityKey -> Janitor) { implicit request =>
  def index = Action { implicit request =>
//    val user = loggedIn
    Ok(views.html.staff.index())
  }

}
