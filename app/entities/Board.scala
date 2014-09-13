package entities

import reactivemongo.bson.BSONObjectID
import reactivemongo.bson

case class Board (
  _id: Option[BSONObjectID],
  name: String,
  fullName: String,
  threads: Option[List[Thread]]
) extends MongoEntity {

  def slashizedName = "/" + name + "/"

}



object Board {

  implicit val boardBSONHandler = bson.Macros.handler[Board]

}
