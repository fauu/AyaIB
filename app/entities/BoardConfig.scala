package entities

import reactivemongo.bson.BSONObjectID
import reactivemongo.bson

case class BoardConfig (
  _id: Option[BSONObjectID] = None,
  allowedContentTypes: List[String]
) extends MongoEntity { }

object BoardConfig {

  implicit val boardConfigBSONHandler = bson.Macros.handler[BoardConfig]

}
