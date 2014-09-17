package entities

import reactivemongo.bson.BSONObjectID
import reactivemongo.bson

case class Post (
  _id: Option[BSONObjectID] = Some(BSONObjectID.generate),
  no: Int,
  content: String,
  fileRef: Option[BSONObjectID] = None,
  thumbnailRef: Option[BSONObjectID] = None
) extends MongoEntity

object Post {

  implicit val postBSONHandler = bson.Macros.handler[Post]

}
