package forms

import play.api.data._
import play.api.data.Forms._

case class PostForm(
  content: String,
  subject: Option[String],
  email: Option[String]
)

object PostForm {

  def get = Form(mapping(
    "content" -> nonEmptyText(maxLength = 1500),
    "subject" -> optional(nonEmptyText(maxLength = 40)),
    "email" -> optional(nonEmptyText(maxLength = 40))
  )(PostForm.apply)(PostForm.unapply))

}
