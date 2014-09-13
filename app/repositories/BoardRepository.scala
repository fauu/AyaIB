package repositories

import entities.Board
import scala.concurrent.Future
import reactivemongo.bson.{Producer, BSONNull, Macros, BSONDocument}
import reactivemongo.bson

trait BoardRepositoryComponent {

  def boardRepository: BoardRepository

  trait BoardRepository extends MongoRepository {
    type A = Board

    def findAllSimple: Future[List[Board]]
    def findByName(name: String): Future[Option[Board]]
  }

}

trait BoardRepositoryComponentImpl extends BoardRepositoryComponent {

  override val boardRepository = new BoardRepositoryImpl

  class BoardRepositoryImpl extends BoardRepository {
    protected val collectionName = "boards"
    protected val bsonDocumentHandler = Board.boardBSONHandler

    def findAllSimple = query(filter = BSONDocument("threads" -> 0))
    def findByName(name: String) = queryOne(BSONDocument("name" -> name))
  }

}