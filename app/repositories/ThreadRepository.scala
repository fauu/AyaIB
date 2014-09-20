package repositories

import scala.concurrent.Future

import entities.{Post, Board, Thread}
import reactivemongo.bson.BSONDocument
import reactivemongo.core.commands._
import com.github.nscala_time.time.Imports.DateTime

trait ThreadRepositoryComponent {

  def threadRepository: ThreadRepository

  trait ThreadRepository extends MongoRepository {

    type A = Thread

    def add(board: Board, thread: Thread): Future[LastError]

    def addReply(board: Board, thread: Thread, post: Post): Future[LastError]

    def findByBoard(board: Board): Future[List[Thread]]

    def findOneByBoardAndNo(board: Board, no: Int): Future[Option[Thread]]

    def updateBumpDate(board: Board, no: Int, date: DateTime): Future[LastError]

  }

}

trait ThreadRepositoryComponentImpl extends ThreadRepositoryComponent {

  override val threadRepository = new ThreadRepositoryImpl

  class ThreadRepositoryImpl extends ThreadRepository {

    protected val collectionName = "threads"
    protected val bsonDocumentHandler = Thread.threadBSONHandler

    def add(board: Board, thread: Thread) = mongoSave(thread.copy(_board_id = board._id))

    def addReply(board: Board, thread: Thread, post: Post) =
      mongoUpdate(BSONDocument("_board_id" -> board._id.get, "op.no" -> thread.op.no),
                  BSONDocument("$push" -> BSONDocument("replies" -> post)))

    def findByBoard(board: Board) =
      mongoFind(BSONDocument("_board_id" -> board._id.get))

    def findOneByBoardAndNo(board: Board, no: Int) =
      mongoFindOne(BSONDocument("_board_id" -> board._id.get, "op.no" -> no))

    def updateBumpDate(board: Board, no: Int, date: DateTime) =
      mongoUpdate(BSONDocument("_board_id" -> board._id, "op.no" -> no),
                  BSONDocument("$set" -> BSONDocument("bumpDate" -> date)))

  }

}
