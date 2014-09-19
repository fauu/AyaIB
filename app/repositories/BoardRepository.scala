package repositories

import entities.Board
import scala.concurrent.Future
import reactivemongo.bson.BSONDocument
import reactivemongo.core.commands.LastError

trait BoardRepositoryComponent {

  def boardRepository: BoardRepository

  trait BoardRepository extends MongoRepository {
    type A = Board

    def findAllSimple: Future[List[Board]]
    def findByName(name: String): Future[Option[Board]]
    def findByNameSimple(name: String): Future[Option[Board]]
    def findByNameWithSingleThread(name: String, threadNo: Int): Future[Option[Board]]
    def incrementLastPostNo(name: String): Future[LastError]
  }

}

trait BoardRepositoryComponentImpl extends BoardRepositoryComponent {

  override val boardRepository = new BoardRepositoryImpl

  class BoardRepositoryImpl extends BoardRepository {
    protected val collectionName = "boards"
    protected val bsonDocumentHandler = Board.boardBSONHandler

    def findAllSimple = query(filter = BSONDocument("threads" -> 0))

    def findByName(name: String) = queryOne(BSONDocument("name" -> name))

    def findByNameSimple(name: String) = queryOne(BSONDocument("name" -> name), BSONDocument("threads" -> 0))

    def findByNameWithSingleThread(name: String, threadNo: Int)
      = queryOne(BSONDocument("threads.op.no" -> threadNo), BSONDocument("threads.$nin" -> true))

    def incrementLastPostNo(name: String)
      = update(BSONDocument("name" -> name), BSONDocument("$inc" -> BSONDocument("lastPostNo" -> 1)))

  }

}