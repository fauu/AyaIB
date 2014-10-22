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
import play.api.libs.functional.syntax._
import play.modules.reactivemongo.json.BSONFormats._

import reactivemongo.bson.BSONObjectID

case class Board (
  _id: Option[BSONObjectID] = Some(BSONObjectID.generate),
  name: String,
  fullName: String,
  lastPostNo: Int = 0,
  config: BoardConfig
) extends MongoEntity {

  def slashizedName = "/%s/".format(name)

}

object Board {

  implicit val jsonFormat = Json.format[Board]

}
