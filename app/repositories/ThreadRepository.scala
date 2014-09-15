package repositories

import entities.Thread
import scala.concurrent.Future
import reactivemongo.bson.BSONDocument
import reactivemongo.core.commands.LastError

trait ThreadRepositoryComponent {

  def threadRepository: ThreadRepository

  trait ThreadRepository extends MongoRepository {
    type A = Thread

    def add(boardName: String, thread: Thread): Future[LastError]
  }

}

trait ThreadRepositoryComponentImpl extends ThreadRepositoryComponent {

  override val threadRepository = new ThreadRepositoryImpl

  class ThreadRepositoryImpl extends ThreadRepository {
    protected val collectionName = "boards"
    protected val bsonDocumentHandler = Thread.threadBSONHandler

    def add(boardName: String, thread: Thread): Future[LastError]
      = upsert(BSONDocument("name" -> boardName),
               BSONDocument("$push" -> BSONDocument("threads" -> thread)))
  }

}
