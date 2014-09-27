package models.entities

import play.api.libs.json._
import play.modules.reactivemongo.json.BSONFormats._

import reactivemongo.bson.BSONObjectID

import utils.json.FormatImplicits._
import auth.Permission

case class StaffMember (
  _id: Option[BSONObjectID] = Some(BSONObjectID.generate),
  id: String,
  password: String,
  permission: Permission
) extends MongoEntity { }

object StaffMember {

  implicit val jsonFormat = Json.format[StaffMember]

}
