/*
 * Copyright (C) 2014 AyaIB Developers (http://github.com/fauu/ayaib)
 *
 * This software is licensed under the GNU General Public License
 * (version 3 or later). See the COPYING file in this distribution.
 *
 * You should have received a copy of the GNU Library General Public License
 * along with this software. If not, see <http://www.gnu.org/licenses/>.
 *
 * Authored by: Piotr Grabowski <fau999@gmail.com>
 */

package models

import anorm._
import play.api.db.DB
import play.api.Play.current

case class Image(
    name: Pk[String] = NotAssigned,
    originalName: String,
    mimeType: String,
    size: String,
    width: Int,
    height: Int,
    isDeleted: Option[Boolean] = None,
    hash: String)

object Image {

  val TableName = "image"

  private val Store =
    """
      INSERT INTO {tableName} (name, origname, mimetype, size, width, height, hash)
      VALUES ({name}, {originalName}, {mimeType}, {size}, {width}, {height}, {hash})
    """.replace("{tableName}", TableName)

  def store(image: Image): Int = {
    DB.withConnection { implicit connection =>
      SQL(Store).on(
        'name -> image.name,
        'originalName -> image.originalName,
        'mimeType -> image.mimeType,
        'size -> image.size,
        'width -> image.width,
        'height -> image.height,
        'hash -> image.hash
      ).executeUpdate()
    }
  }

}
