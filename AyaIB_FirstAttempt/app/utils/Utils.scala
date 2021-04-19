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

package utils

import com.github.nscala_time.time.Imports._
import play.api.Play
import play.api.mvc.Results._
import java.awt.Dimension

object Utils {

  val imageDir = Play.current.configuration.getString("ayaib.uploadDir")
    .getOrElse(InternalServerError)

  private val contentTypesExtensions = Map("image/jpeg" -> "jpg", "image/jpg" -> "jpg",
                                           "image/png" -> "png", "image/gif" -> "gif")

  def contentTypeToExtension(contentType: String) = contentTypesExtensions.get(contentType).get

  def extensionToContentType(extension: String) = contentTypesExtensions.map(_.swap).get(extension).get

  def formatDate(date: DateTime, pattern: String) =
    DateTimeFormat.forPattern(pattern).print(date)

  def humanizeFileSize(sizeBytes: Long) =
    Map(1073741824 -> "GB", 1048576 -> "MB", 1024 -> "KB", 1 -> " bytes")
    .dropWhile(unitSize => sizeBytes < unitSize._1).take(1).map( unitSize =>
        new java.text.DecimalFormat("#.##").format(sizeBytes.toFloat / unitSize._1) + " " + unitSize._2
    ).head

  def bytesToHex(bytes: Array[Byte]) = bytes.map("%02x".format(_)).mkString

  def scaleDimensions(source: (Int, Int), boundary: (Int, Int)) = {
    val scaling = (boundary._1 / source._1.toDouble, boundary._2 / source._2.toDouble)

    if (scaling._1 < scaling._2)
      ((source._1 * scaling._1).toInt, (source._2 * scaling._1).toInt)
    else
      ((source._1 * scaling._2).toInt, (source._2 * scaling._2).toInt)
  }

}
