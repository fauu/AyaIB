/*
 * Copyright (C) 2014 AyaIB Developers (http://github.com/fauu/ayaib)
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

import play.api.mvc._
import java.io.{FileNotFoundException, FileInputStream, BufferedInputStream}

object Images extends Controller {

  def view(filename: String) = Action {
    val contentType = utils.Utils.extensionToContentType(filename.dropWhile(_ != '.').drop(1))

    try {
      val filePath = utils.Utils.imageDir + filename
      val bis = new BufferedInputStream(new FileInputStream(filePath))
      val imageData = Stream.continually(bis.read()).takeWhile(-1 !=).map(_.toByte).toArray

      Ok(imageData).as(contentType)
    } catch {
      case e: IllegalArgumentException => BadRequest
      case e: FileNotFoundException => NotFound
    }
  }

}
