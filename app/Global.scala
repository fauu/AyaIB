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

import play.api._
import play.api.mvc.Results._
import play.api.mvc._
import scala.concurrent.Future

object Global extends GlobalSettings {

  override def onStart(app: Application) =
    Logger.info("Application has started")

  override def onStop(app: Application) =
    Logger.info("Application shutdown...")

  override def onHandlerNotFound(request: RequestHeader) =
    Future.successful(NotFound(views.html.notFound()))

}
