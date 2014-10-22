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

import reactivemongo.core.commands._

import com.github.nscala_time.time.Imports.DateTime

import utils.json.FormatImplicits._
import models.entities.{Post, Board, Thread}
import reactivemongo.bson.{BSONDocument, BSONObjectID}

trait ThreadRepositoryComponent {

  def threadRepository: ThreadRepository

  trait ThreadRepository extends MongoRepository {

    type A = Thread

    def add(board: Board, thread: Thread): Future[LastError]

    def addReply(board: Board, thread: Thread, post: Post): Future[LastError]

    def findCountByBoard(board: Board): Future[Int]

    // Well, this is going out of hand
    def findExcerptsByBoardSortedByBumpDateDescLimited(board: Board,
                                                       maxNumReplies: Int = 3,
                                                       start: Int = 0,
                                                       count: Int = 15): Future[List[Thread]]

    def findOneByBoardAndNo(board: Board, no: Int): Future[Option[Thread]]

    def findOneByBoardAndPostNo(board: Board, no: Int): Future[Option[Thread]]

    def findOneByBoardAndOpNo(board: Board, no: Int): Future[Option[Thread]]

    def findOneByBoardAndReplyNo(board: Board, no: Int): Future[Option[Thread]]

    def incrementNumReplies(board: Board, no: Int): Future[LastError]

    def updateBumpDate(board: Board, no: Int, date: DateTime): Future[LastError]

    def updateOp(board: Board, threadNo: Int, op: Post): Future[LastError]

    def updateReply(board: Board, replyNo: Int, reply: Post): Future[LastError]

  }

}

trait ThreadRepositoryComponentImpl extends ThreadRepositoryComponent {

  override val threadRepository = new ThreadRepositoryImpl

  class ThreadRepositoryImpl extends ThreadRepository {

    protected val collectionName = "threads"
    protected val jsonFormat = Thread.jsonFormat

    def add(board: Board, thread: Thread) = mongoSave(thread copy (_board_id = board._id))

    def addReply(board: Board, thread: Thread, post: Post) =
      mongoUpdate(Json.obj("_board_id" -> board._id.get, "op.no" -> thread.op.no),
                  Json.obj("$push" -> Json.obj("replies" -> post)))

    def findCountByBoard(board: Board): Future[Int] = mongoCount(BSONDocument("_board_id" -> board._id))

    def findExcerptsByBoardSortedByBumpDateDescLimited(board: Board,
                                                       maxNumReplies: Int = 3,
                                                       start: Int = 0,
                                                       count: Int = 15) =
      mongoFindSortedAndLimited(Json.obj("_board_id" -> board._id.get),
                                projection = Json.obj("replies" -> Json.obj("$slice" -> -1 * maxNumReplies)),
                                sort = Json.obj("bumpDate" -> -1),
                                start = start,
                                count = count)

    def findOneByBoardAndNo(board: Board, no: Int) =
      mongoFindOne(Json.obj("_board_id" -> board._id.get, "op.no" -> no))

    def findOneByBoardAndPostNo(board: Board, no: Int) =
      mongoFindOne(Json.obj("_board_id" -> board._id.get, "$or" -> Json.arr(Json.obj("op.no" -> no),
                                                                            Json.obj("replies.no" -> no))))
    def findOneByBoardAndReplyNo(board: Board, no: Int) =
      mongoFindOne(Json.obj("_board_id" -> board._id.get, "replies.no" -> no))

    def findOneByBoardAndOpNo(board: Board, no: Int) =
      mongoFindOne(Json.obj("_board_id" -> board._id.get, "op.no" -> no))

    def incrementNumReplies(board: Board, no: Int) =
      mongoUpdate(Json.obj("_board_id" -> board._id.get, "op.no" -> no),
                  Json.obj("$inc" -> Json.obj("numReplies" -> 1)))

    def updateBumpDate(board: Board, no: Int, date: DateTime) =
      mongoUpdate(Json.obj("_board_id" -> board._id, "op.no" -> no),
                  Json.obj("$set" -> Json.obj("bumpDate" -> date)))

    def updateReply(board: Board, replyNo: Int, reply: Post) =
      mongoUpdate(Json.obj("_board_id" -> board._id, "replies.no" -> replyNo),
                  Json.obj("$set" -> Json.obj("replies.$" -> reply)))

    def updateOp(board: Board, threadNo: Int, op: Post) =
      mongoUpdate(Json.obj("_board_id" -> board._id, "op.no" -> threadNo),
                  Json.obj("$set" -> Json.obj("op" -> op)))

  }

}
