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

import utils.json.FormatImplicits._

import com.github.nscala_time.time.Imports.DateTime

case class Quotation (
  _id: Option[BSONObjectID] = Some(BSONObjectID.generate),
  _sourceBoard_id: Option[BSONObjectID],
  sourceNo: Int,
  _targetBoard_id: Option[BSONObjectID],
  targetNo: Int
) extends MongoEntity { }

object Quotation {

  implicit val jsonFormat = Json.format[Quotation]

}
