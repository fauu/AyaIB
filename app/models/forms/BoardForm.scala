package models.forms

import play.api.data._
import play.api.data.Forms._

case class BoardForm(
  name: String = "",
  fullName: String = "",
  allowedContentTypesStr: String = "image/jpeg;image/png;image/gif",
  maxNumPages: Int = 10,
  threadsPerPage: Int = 15
)

object BoardForm {

  def get = Form(mapping(
    "name" -> nonEmptyText(maxLength = 15),
    "fullName" -> nonEmptyText(maxLength = 40),
    "allowedContentTypesStr" -> nonEmptyText(maxLength = 500),
    "maxNumPages" -> number(min = 1, max = 50),
    "threadsPerPage" -> number(min = 1, max = 25)
  )(BoardForm.apply)(BoardForm.unapply))

}
