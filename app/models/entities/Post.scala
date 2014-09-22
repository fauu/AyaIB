package models.entities

import play.api.libs.json.Json
import play.modules.reactivemongo.json.BSONFormats._

import reactivemongo.bson.BSONObjectID

import com.github.nscala_time.time.Imports.DateTime

case class Post (
  _id: Option[BSONObjectID] = Some(BSONObjectID.generate),
  no: Int,
  date: DateTime,
  subject: Option[String] = None,
  name: Option[String] = None,
  email: Option[String] = None,
  content: String,
  fileName: Option[String] = None,
  fileMetadata: Option[FileMetadata] = None,
  thumbnailName: Option[String] = None
) extends MongoEntity { }

object Post {

  implicit val jsonFormat = Json.format[Post]
  implicit val fileMetadataJsonFormat = FileMetadata.jsonFormat;

}
