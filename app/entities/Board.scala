package entities

import reactivemongo.bson
import reactivemongo.bson.BSONObjectID

case class Board (
  _id: Option[BSONObjectID] = None,
  name: String,
  fullName: String,
  lastPostNo: Int = 0,
  config: BoardConfig,
  threads: Option[List[Thread]]
) extends MongoEntity {

  def slashizedName = "/" + name + "/"

}

object Board {

  implicit val boardBSONHandler = bson.Macros.handler[Board]

}
