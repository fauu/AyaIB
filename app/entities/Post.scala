package entities

import reactivemongo.bson
import reactivemongo.bson.{BSONWriter, BSONDateTime, BSONReader, BSONObjectID}
import com.github.nscala_time.time.Imports.DateTime

case class Post (
  _id: Option[BSONObjectID] = Some(BSONObjectID.generate),
  no: Int,
  date: DateTime,
  subject: Option[String] = None,
  email: Option[String] = None,
  content: String,
  fileName: Option[String] = None,
  fileMetadata: Option[FileMetadata] = None,
  thumbnailName: Option[String] = None
) extends MongoEntity

object Post {

  // TODO: Factor this out
  implicit object DateTimeReader extends BSONReader[BSONDateTime, DateTime] {
    def read(bson: BSONDateTime): DateTime = new DateTime(bson.value)
  }

  implicit object DateTimeWriter extends BSONWriter[DateTime, BSONDateTime] {
    def write(t: DateTime): BSONDateTime = BSONDateTime(t.getMillis)
  }

  implicit val postBSONHandler = bson.Macros.handler[Post]

}
