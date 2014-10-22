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
import scala.concurrent.ExecutionContext.Implicits.global

import play.api.libs.json.Json
import play.modules.reactivemongo.json.BSONFormats._

import reactivemongo.core.commands._

import utils.json.FormatImplicits._
import models.entities.{Board, PostId}

trait PostIdRepositoryComponent {

  def postIdRepository: PostIdRepository

  trait PostIdRepository extends MongoRepository {

    type A = PostId

    def add(board: Board, postId: PostId): Future[LastError]

    def findOne(board: Board, postNo: Int): Future[Option[PostId]]

  }

}

trait PostIdRepositoryComponentImpl extends PostIdRepositoryComponent {

  override val postIdRepository = new PostIdRepositoryImpl

  class PostIdRepositoryImpl extends PostIdRepository {

    protected val collectionName = "postIds"
    protected val jsonFormat = PostId.jsonFormat

    def add(board: Board, postId: PostId) = mongoSave(postId copy (_board_id = board._id))

    def findOne(board: Board, postNo: Int) = mongoFindOne(Json.obj("_board_id" -> board._id, "no" -> postNo))

  }

}
