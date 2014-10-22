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

import play.api.libs.json.Json
import play.modules.reactivemongo.json.BSONFormats._

import reactivemongo.bson.BSONObjectID

case class FileMetadata (
  _id: Option[BSONObjectID] = Some(BSONObjectID.generate),
  originalName: String,
  dimensions: String,
  size: String
) extends MongoEntity { }

object FileMetadata {

  implicit val jsonFormat = Json.format[FileMetadata]

}
