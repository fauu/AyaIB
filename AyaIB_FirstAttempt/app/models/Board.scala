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
import anorm.SqlParser._
import play.api.db.DB
import play.api.Play.current

case class Board(
    id: Pk[Int] = NotAssigned,
    uri: String,
    title: String,
    subtitle: Option[String] = None,
    numPages: Int)

object Board {

  val TableName = "board"

  private val LoadAll =
    "SELECT * FROM {tableName}".replace("{tableName}", TableName)

  private val LoadByUri =
    "SELECT * FROM {tableName} WHERE uri = {uri}".replace("{tableName}", TableName)

  private val boardParser = {
    get[Pk[Int]]("id") ~
    get[String]("uri") ~
    get[String]("title") ~
    get[Option[String]]("subtitle") ~
    get[Int]("pages") map {
      case id ~ uri ~ title ~ subtitle ~ numPages => Board(id, uri, title, subtitle, numPages)
    }
  }

  def loadAll: List[Board] = {
    DB.withConnection { implicit connection =>
      SQL(LoadAll).as(boardParser *).toList
    }
  }

  def loadByUri(uri: String): Option[Board] = {
    DB.withConnection { implicit connection =>
        SQL(LoadByUri).on('uri -> uri).as(boardParser.singleOpt)
    }
  }

}


