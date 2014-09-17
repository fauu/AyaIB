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

    def setOpFileRefs(boardName: String, threadId: Option[BSONObjectID], fileId: BSONValue, thumbnailId: BSONValue)
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

    def setOpFileRefs(boardName: String, threadId: Option[BSONObjectID], fileId: BSONValue, thumbnailId: BSONValue)
      = update(BSONDocument("name" -> boardName, "threads._id" -> threadId.get),
               BSONDocument("$set" -> BSONDocument("threads.$.op.fileRef" -> fileId,
                                                   "threads.$.op.thumbnailRef" -> thumbnailId)))
  }

}
