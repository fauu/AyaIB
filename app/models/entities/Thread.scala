package models.entities

import play.api.libs.json.Json
import play.modules.reactivemongo.json.BSONFormats._

import reactivemongo.bson.BSONObjectID

import com.github.nscala_time.time.Imports.DateTime

case class Thread (
  _id: Option[BSONObjectID] = Some(BSONObjectID.generate),
  _board_id: Option[BSONObjectID] = None,
  bumpDate: DateTime,
  op: Post,
  replies: List[Post] = List[Post]()
) extends MongoEntity { }

object Thread {

  implicit val jsonFormat = Json.format[Thread]
  implicit val postJsonFormat = Post.jsonFormat

}
