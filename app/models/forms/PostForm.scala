package models.forms

import play.api.data._
import play.api.data.Forms._

case class PostForm(
  subject: Option[String],
  name: Option[String],
  email: Option[String],
  content: String
)

object PostForm {

  def get = Form(mapping(
    "subject" -> optional(nonEmptyText(maxLength = 40)),
    "name" -> optional(nonEmptyText(maxLength = 40)),
    "email" -> optional(nonEmptyText(maxLength = 40)),
    "content" -> nonEmptyText(maxLength = 1500)
  )(PostForm.apply)(PostForm.unapply))

}
