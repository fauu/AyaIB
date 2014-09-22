package entities

import play.api.libs.json.Json
import play.modules.reactivemongo.json.BSONFormats._

import reactivemongo.bson.BSONObjectID

case class BoardConfig (
  _id: Option[BSONObjectID] = Some(BSONObjectID.generate),
  allowedContentTypes: List[String]
) extends MongoEntity { }

object BoardConfig {

  implicit val jsonFormat = Json.format[BoardConfig]

}
