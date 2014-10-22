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

package models.entities

import play.api.libs.json._
import play.modules.reactivemongo.json.BSONFormats._

import reactivemongo.bson.BSONObjectID

import com.github.nscala_time.time.Imports.DateTime

import utils.json.FormatImplicits._

case class Post (
  _id: Option[BSONObjectID] = Some(BSONObjectID.generate),
  no: Int,
  date: DateTime,
  subject: Option[String] = None,
  name: Option[String] = None,
  email: Option[String] = None,
  content: String,
  fileName: Option[String] = None,
  fileMetadata: Option[FileMetadata] = None,
  thumbnailName: Option[String] = None
) extends MongoEntity {

  def contentPreview(length: Int): String =
    if (content.length <= length) content
    else "%s…" format (content take (content lastIndexWhere (_.isSpaceChar, length + 1))).trim

}

object Post {

  implicit val jsonFormat = Json.format[Post]

}
