package models.forms

import play.api.data.Form
import play.api.data.Forms._

case class StaffLoginForm (
  id: String,
  password: String
)

object StaffLoginForm {

  def get = Form(mapping(
    "id"-> text,
    "password" -> text
  )(StaffLoginForm.apply)(StaffLoginForm.unapply))

}
