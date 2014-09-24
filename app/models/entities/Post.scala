package models.entities

import play.api.libs.json._
import play.modules.reactivemongo.json.BSONFormats._

import reactivemongo.bson.BSONObjectID

import com.github.nscala_time.time.Imports.DateTime

import utils.json.FormatImplicits._

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
) extends MongoEntity {

  def contentPreview(length: Int): String =
    if (content.length <= length) content
    else "%s…" format (content take (content lastIndexWhere (_.isSpaceChar, length + 1))).trim

}

object Post {

  implicit val jsonFormat = Json.format[Post]

}
