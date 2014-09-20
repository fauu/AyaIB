package repositories

import scala.concurrent.Future

import entities.Board
import reactivemongo.bson.BSONDocument
import reactivemongo.core.commands.LastError

trait BoardRepositoryComponent {

  def boardRepository: BoardRepository

  trait BoardRepository extends MongoRepository {

    type A = Board

    def findAll: Future[List[Board]]

    def findOneByName(name: String): Future[Option[Board]]

    def incrementLastPostNo(name: String): Future[LastError]

  }

}

trait BoardRepositoryComponentImpl extends BoardRepositoryComponent {

  override val boardRepository = new BoardRepositoryImpl

  class BoardRepositoryImpl extends BoardRepository {

    protected val collectionName = "boards"
    protected val bsonDocumentHandler = Board.boardBSONHandler

    def findAll = mongoFind()

    def findOneByName(name: String) = mongoFindOne(BSONDocument("name" -> name))

    def incrementLastPostNo(name: String) =
      mongoUpdate(BSONDocument("name" -> name), BSONDocument("$inc" -> BSONDocument("lastPostNo" -> 1)))

  }

}