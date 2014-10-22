/*
 * Copyright (C) 2014 AyaIB Developers (http://github.com/fauu/AyaIB)
 *
 * This software is licensed under the GNU General Public License
 * (version 3 or later). See the COPYING file in this distribution.
 *
 * You should have received a copy of the GNU Library General Public License
 * along with this software. If not, see <http://www.gnu.org/licenses/>.
 *
 * Authored by: Piotr Grabowski <fau999@gmail.com>
 */

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
