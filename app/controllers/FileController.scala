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

import scala.concurrent.ExecutionContext.Implicits._

import play.api.mvc.{Action, Controller, ResponseHeader, Result}

import context.AyaIBContext

object FileController extends Controller {

  val fileService = AyaIBContext.fileService

  def get(name: String, thumbnail: Boolean) = Action.async {
    fileService findByName (name, thumbnail) map {
      case Some((file, enumerator)) =>
        Result(
          header = ResponseHeader(OK, Map(
            CONTENT_LENGTH -> file.length.toString,
            CONTENT_DISPOSITION -> (s"""inline; filename="${file.filename}"; filename*=UTF-8''"""  +
                                    java.net.URLEncoder.encode(file.filename, "UTF-8").replace("+", "%20")),
            CONTENT_TYPE -> file.contentType.getOrElse("application/octet-stream"))),
          body = enumerator
        )
      case _ => NotFound(views.html.notFound())
    }
  }

}
