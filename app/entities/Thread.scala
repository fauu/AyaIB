package entities

import reactivemongo.bson.BSONObjectID
import reactivemongo.bson
import entities.Post

case class Thread (
  _id: Option[BSONObjectID],
  op: Post,
  replies: List[Post]
) extends MongoEntity

object Thread {

  implicit val threadBSONHandler = bson.Macros.handler[Thread]

}
