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

package auth

import jp.t2v.lab.play2.auth.AuthConfig
import models.entities.StaffMember
import scala.reflect.classTag
import scala.concurrent.{ExecutionContext, Future}
import play.api.mvc.{Result, RequestHeader}
import controllers.routes
import context.AyaIBContext
import play.api.mvc.Results.{Redirect, Forbidden}

trait AuthConfigImpl extends AuthConfig {

  type Id = String

  type User = StaffMember

  type Authority = Permission

  val staffService = AyaIBContext.staffService

  val idTag = classTag[Id]

  val sessionTimeoutInSeconds = 5 * 60

  def resolveUser(id: Id)(implicit ctx: ExecutionContext): Future[Option[User]] = staffService.findMemberById(id)

  def loginSucceeded(request: RequestHeader)(implicit ctx: ExecutionContext): Future[Result] =
    Future.successful(Redirect(routes.StaffPanelController.index))

  def logoutSucceeded(request: RequestHeader)(implicit ctx: ExecutionContext): Future[Result] =
    Future.successful(Redirect(routes.ImageboardController.index))

  def authenticationFailed(request: RequestHeader)(implicit ctx: ExecutionContext): Future[Result] =
    Future.successful(Redirect(routes.StaffAuthController.showLoginForm))

  def authorizationFailed(request: RequestHeader)(implicit ctx: ExecutionContext): Future[Result] =
    Future.successful(Forbidden)

  def authorize(user: User, authority: Authority)(implicit ctx: ExecutionContext): Future[Boolean] =
    Future.successful(user.permission.level >= authority.level)

  override lazy val cookieSecureOption: Boolean = play.api.Play.isProd(play.api.Play.current)

}
