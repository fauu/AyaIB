package entities

import reactivemongo.bson.BSONObjectID
import reactivemongo.bson

case class Board (
  _id: Option[BSONObjectID] = None,
  name: String,
  fullName: String,
  lastPostNo: Int,
  threads: Option[List[Thread]]
) extends MongoEntity {

  def slashizedName = "/" + name + "/"

}



object Board {

  implicit val boardBSONHandler = bson.Macros.handler[Board]

}
