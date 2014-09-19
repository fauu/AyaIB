package forms

import play.api.data._
import play.api.data.Forms._

case class PostForm(
  content: String
)

object PostForm {

  def get = Form(mapping(
      "content" -> nonEmptyText(maxLength = 1800)
  )(PostForm.apply)(PostForm.unapply))

}
