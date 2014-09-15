package entities

import reactivemongo.bson
import reactivemongo.bson.BSONObjectID

case class FileMetadata (
  _id: Option[BSONObjectID],
  originalName: String,
  dimensions: String
) extends MongoEntity { }

object FileMetadata {

  implicit val fileMetadataBSONHandler = bson.Macros.handler[FileMetadata]

}
