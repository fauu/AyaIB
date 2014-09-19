package entities

import reactivemongo.bson.BSONObjectID
import reactivemongo.bson

case class Thread (
  _id: Option[BSONObjectID] = Some(BSONObjectID.generate),
  op: Post,
  replies: List[Post] = List[Post]()
) extends MongoEntity

object Thread {

  implicit val threadBSONHandler = bson.Macros.handler[Thread]

}
