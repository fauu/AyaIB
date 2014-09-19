package entities

import reactivemongo.bson
import reactivemongo.bson.BSONObjectID

case class BoardConfig (
  _id: Option[BSONObjectID] = None,
  allowedContentTypes: List[String]
) extends MongoEntity { }

object BoardConfig {

  implicit val boardConfigBSONHandler = bson.Macros.handler[BoardConfig]

}
