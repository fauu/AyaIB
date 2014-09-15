package entities

import reactivemongo.bson.BSONObjectID
import reactivemongo.bson

case class Thread (
  _id: Option[BSONObjectID] = None,
  op: Post,
  replies: List[Post]
) extends MongoEntity

object Thread {

  implicit val threadBSONHandler = bson.Macros.handler[Thread]

}
