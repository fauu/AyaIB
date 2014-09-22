package models.entities

import play.api.libs.json.Json
import play.modules.reactivemongo.json.BSONFormats._

import reactivemongo.bson.BSONObjectID

case class FileMetadata (
  _id: Option[BSONObjectID] = Some(BSONObjectID.generate),
  originalName: String,
  dimensions: String,
  size: String
) extends MongoEntity { }

object FileMetadata {

  implicit val jsonFormat = Json.format[FileMetadata]

}
