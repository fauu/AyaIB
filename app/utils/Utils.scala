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

package utils

import java.text.DecimalFormat
import com.github.nscala_time.time.Imports.{DateTime, DateTimeFormat}

object Utils {

  private lazy val fileLengthConversionThresholds =
    List((1073741824, "GB", "#.##"),
         (1048576, "MB", "#.##"),
         (1024, "KB", "#"),
         (1, "bytes", "#"))

  private lazy val contentTypesExtensions =
     Map("image/jpeg" -> "jpg",
         "image/png" -> "png",
         "image/gif" -> "gif")

  def humanizeFileLength(length: Long): String =
    (fileLengthConversionThresholds dropWhile (_._1 > length) take 1 map { threshold =>
      (new DecimalFormat(threshold._3) format (length.toFloat / threshold._1)) + " " + threshold._2
    }).head

  def contentTypeToExtension(contentType: String): Option[String] = contentTypesExtensions.get(contentType)

  def formatDate(date: DateTime, pattern: String = "yyyy-MM-dd H:mm:ss") =
    DateTimeFormat.forPattern(pattern) print date

  def stringToInt(s: String): Option[Int] =
    try {
      Some(s.toInt)
    } catch {
      case e: Exception => None
    }

}
