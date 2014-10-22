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

package repositories

import scala.concurrent.Future

import play.api.libs.json.Json
import play.modules.reactivemongo.json.BSONFormats._

import reactivemongo.core.commands.LastError

import utils.json.FormatImplicits._
import models.entities.Board
import reactivemongo.bson.BSONObjectID

trait BoardRepositoryComponent {

  def boardRepository: BoardRepository

  trait BoardRepository extends MongoRepository {

    type A = Board

    def add(board: Board): Future[LastError]

    def findAll: Future[List[Board]]

    def findOne(_id: BSONObjectID): Future[Option[Board]]

    def findOneByName(name: String): Future[Option[Board]]

    def incrementLastPostNo(name: String): Future[LastError]

  }

}

trait BoardRepositoryComponentImpl extends BoardRepositoryComponent {

  override val boardRepository = new BoardRepositoryImpl

  class BoardRepositoryImpl extends BoardRepository {

    protected val collectionName = "boards"
    protected val jsonFormat = Board.jsonFormat

    def add(board: Board) = mongoSave(board)

    def findAll = mongoFind()

    def findOne(_id: BSONObjectID) = mongoFindOne(Json.obj("_id" -> _id))

    def findOneByName(name: String) = mongoFindOne(Json.obj("name" -> name))

    def incrementLastPostNo(name: String) =
      mongoUpdate(Json.obj("name" -> name), Json.obj("$inc" -> Json.obj("lastPostNo" -> 1)))

  }

}