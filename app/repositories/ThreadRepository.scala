package repositories

import entities.Thread
import scala.concurrent.Future
import reactivemongo.bson.{BSONValue, BSONObjectID, BSONDocument}
import reactivemongo.core.commands.LastError

trait ThreadRepositoryComponent {

  def threadRepository: ThreadRepository

  trait ThreadRepository extends MongoRepository {
    type A = Thread

    def add(boardName: String, thread: Thread): Future[LastError]

    def setOpFilenames(boardName: String, threadId: Option[BSONObjectID], fileName: String, thumbnailName: String)
      : Future[LastError]
  }

}

trait ThreadRepositoryComponentImpl extends ThreadRepositoryComponent {

  override val threadRepository = new ThreadRepositoryImpl

  class ThreadRepositoryImpl extends ThreadRepository {
    protected val collectionName = "boards"
    protected val bsonDocumentHandler = Thread.threadBSONHandler

    def add(boardName: String, thread: Thread): Future[LastError]
      = update(BSONDocument("name" -> boardName),
               BSONDocument("$push" -> BSONDocument("threads" -> thread)))

    def setOpFilenames(boardName: String, threadId: Option[BSONObjectID], fileName: String, thumbnailName: String)
      = update(BSONDocument("name" -> boardName, "threads._id" -> threadId.get),
               BSONDocument("$set" -> BSONDocument("threads.$.op.fileName" -> fileName,
                                                   "threads.$.op.thumbnailName" -> thumbnailName)))
  }

}
