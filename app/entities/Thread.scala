package entities

import reactivemongo.bson
import reactivemongo.bson.BSONObjectID

case class Thread (
  _id: Option[BSONObjectID] = Some(BSONObjectID.generate),
  op: Post,
  replies: List[Post] = List[Post]()
) extends MongoEntity

object Thread {

  implicit val threadBSONHandler = bson.Macros.handler[Thread]

}
