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
