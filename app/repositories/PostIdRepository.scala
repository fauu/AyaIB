package repositories

import scala.concurrent.Future

import play.api.libs.json.Json
import play.modules.reactivemongo.json.BSONFormats._

import reactivemongo.core.commands._

import models.entities.{Board, PostId}

trait PostIdRepositoryComponent {

  def postIdRepository: PostIdRepository

  trait PostIdRepository extends MongoRepository {

    type A = PostId

    def add(board: Board, postId: PostId): Future[LastError]

  }

}

trait PostIdRepositoryComponentImpl extends PostIdRepositoryComponent {

  override val postIdRepository = new PostIdRepositoryImpl

  class PostIdRepositoryImpl extends PostIdRepository {

    protected val collectionName = "postIds"
    protected val jsonFormat = PostId.jsonFormat

    def add(board: Board, postId: PostId) = mongoSave(postId copy (_board_id = board._id))

  }

}
