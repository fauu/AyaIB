package repositories

import scala.concurrent.Future

import play.api.libs.json.Json

import reactivemongo.core.commands.LastError

import entities.Board

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
    protected val jsonFormat = Board.jsonFormat

    def findAll = mongoFind()

    def findOneByName(name: String) = mongoFindOne(Json.obj("name" -> name))

    def incrementLastPostNo(name: String) =
      mongoUpdate(Json.obj("name" -> name), Json.obj("$inc" -> Json.obj("lastPostNo" -> 1)))

  }

}