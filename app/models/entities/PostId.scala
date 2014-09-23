package models.entities

import play.api.libs.json.Json
import play.modules.reactivemongo.json.BSONFormats._

import reactivemongo.bson.BSONObjectID

case class PostId (
  _id: Option[BSONObjectID] = Some(BSONObjectID.generate),
  _board_id: Option[BSONObjectID] = None,
  no: Int
) extends MongoEntity { }

object PostId {

  implicit val jsonFormat = Json.format[PostId]

}
