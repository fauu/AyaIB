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

import utils.json.FormatImplicits._

case class PostId (
  _id: Option[BSONObjectID] = Some(BSONObjectID.generate),
  _board_id: Option[BSONObjectID] = None,
  threadNo: Int,
  no: Int
) extends MongoEntity { }

object PostId {

  implicit val jsonFormat = Json.format[PostId]

}
