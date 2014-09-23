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
