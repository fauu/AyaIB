package entities

import reactivemongo.bson
import reactivemongo.bson.BSONObjectID

case class Post (
  _id: Option[BSONObjectID] = Some(BSONObjectID.generate),
  no: Int,
  content: String,
  fileName: Option[String] = None,
  fileMetadata: Option[FileMetadata] = None,
  thumbnailName: Option[String] = None
) extends MongoEntity

object Post {

  implicit val postBSONHandler = bson.Macros.handler[Post]

}
