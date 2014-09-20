package entities

import reactivemongo.bson
import reactivemongo.bson.{BSONWriter, BSONDateTime, BSONReader, BSONObjectID}
import com.github.nscala_time.time.Imports.DateTime

case class Thread (
  _id: Option[BSONObjectID] = Some(BSONObjectID.generate),
  _board_id: Option[BSONObjectID] = None,
  bumpDate: DateTime,
  op: Post,
  replies: List[Post] = List[Post]()
) extends MongoEntity

object Thread {

  // TODO: Factor this out
  implicit object DateTimeReader extends BSONReader[BSONDateTime, DateTime] {
    def read(bson: BSONDateTime): DateTime = new DateTime(bson.value)
  }

  implicit object DateTimeWriter extends BSONWriter[DateTime, BSONDateTime] {
    def write(t: DateTime): BSONDateTime = BSONDateTime(t.getMillis)
  }

  implicit val threadBSONHandler = bson.Macros.handler[Thread]

}
