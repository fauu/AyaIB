package repositories

import scala.concurrent.Future

import entities.Post
import reactivemongo.bson.BSONDocument
import reactivemongo.core.commands._

trait PostRepositoryComponent {

  def postRepository: PostRepository

  trait PostRepository extends MongoRepository {

    type A = Post

    def add(boardName: String, threadNo: Int, post: Post): Future[LastError]

  }

}

trait PostRepositoryComponentImpl extends PostRepositoryComponent {

  override val postRepository = new PostRepositoryImpl

  class PostRepositoryImpl extends PostRepository {

    protected val collectionName = "boards"
    protected val bsonDocumentHandler = Post.postBSONHandler

    def add(boardName: String, threadNo: Int, post: Post) =
      update(BSONDocument("name" -> boardName, "threads.op.no" -> threadNo),
             BSONDocument("$push" -> BSONDocument("threads.$.replies" -> post)))

  }

}
