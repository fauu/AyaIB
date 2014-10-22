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
