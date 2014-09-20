package entities

import reactivemongo.bson
import reactivemongo.bson.BSONObjectID

case class Board (
  _id: Option[BSONObjectID] = None,
  name: String,
  fullName: String,
  lastPostNo: Int = 0,
  config: BoardConfig
) extends MongoEntity {

  def slashizedName = "/%s/".format(name)

}

object Board {

  implicit val boardBSONHandler = bson.Macros.handler[Board]

}
