package utils.json

import play.api.libs.json._

import com.github.nscala_time.time.Imports.{DateTime, DateTimeZone}

import models.entities.{PostId, Post, BoardConfig, FileMetadata}

object FormatImplicits {

  implicit val boardConfigJsonFormat = BoardConfig.jsonFormat
  implicit val fileMetadataJsonFormat = FileMetadata.jsonFormat
  implicit val postJsonFormat = Post.jsonFormat
  implicit val postIdJsonFormat = PostId.jsonFormat

  implicit def dateTimeReads: Reads[DateTime] =
    (__ \ "$date").read[Long].map { dateTime =>
      new DateTime(dateTime, DateTimeZone.UTC)
    }

  implicit def dateTimeWrites: Writes[DateTime] = new Writes[DateTime] {
    def writes(dt: DateTime): JsValue = Json.obj("$date" -> dt.getMillis)
  }

  /* https://github.com/ReactiveMongo/Play-ReactiveMongo/issues/33 */
  implicit def intWrites: Writes[Int] = new Writes[Int] {
    def writes(n: Int): JsValue = Json.obj("$int" -> JsNumber(n))
  }

}
