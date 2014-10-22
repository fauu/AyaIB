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

package utils.json

import play.api.libs.json._

import com.github.nscala_time.time.Imports.{DateTime, DateTimeZone}

import models.entities.{PostId, Post, BoardConfig, FileMetadata}
import auth.{Moderator, Janitor, Administrator, Permission}

object FormatImplicits {

  implicit val boardConfigJsonFormat = BoardConfig.jsonFormat
  implicit val fileMetadataJsonFormat = FileMetadata.jsonFormat
  implicit val postJsonFormat = Post.jsonFormat
  implicit val postIdJsonFormat = PostId.jsonFormat

  implicit def dateTimeReads: Reads[DateTime] =
    (__ \ "$date").read[Long] map { dateTime =>
      new DateTime(dateTime, DateTimeZone.UTC)
    }

  implicit def dateTimeWrites: Writes[DateTime] = new Writes[DateTime] {
    def writes(dt: DateTime): JsValue = Json.obj("$date" -> dt.getMillis)
  }

  /* https://github.com/ReactiveMongo/Play-ReactiveMongo/issues/33 */
  implicit def intWrites: Writes[Int] = new Writes[Int] {
    def writes(n: Int): JsValue = Json.obj("$int" -> JsNumber(n))
  }

  implicit def permissionReads: Reads[Permission] =
    __.read[String] map (permissionStr => Permission.valueOf(permissionStr))

  implicit def permissionWrites: Writes[Permission] = new Writes[Permission] {
    def writes(permission: Permission): JsValue = JsString(permission.toString())
  }

}
