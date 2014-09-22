package entities

import play.api.libs.json.Json
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
  implicit val boardConfigJsonFormat = BoardConfig.jsonFormat

}
