package entities

import reactivemongo.bson.BSONObjectID
import reactivemongo.bson

case class Post (
  _id: Option[BSONObjectID],
  no: Int,
  content: String
) extends MongoEntity

object Post {

  implicit val postBSONHandler = bson.Macros.handler[Post]

}
