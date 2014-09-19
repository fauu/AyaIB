package repositories

import entities.Thread
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import reactivemongo.bson.{BSONInteger, BSONArray, BSONObjectID, BSONDocument}
import reactivemongo.core.commands._
import reactivemongo.core.commands.Unwind

trait ThreadRepositoryComponent {

  def threadRepository: ThreadRepository

  trait ThreadRepository extends MongoRepository {
    type A = Thread

    def findByBoardNameAndNo(boardName: String, no: Int): Future[Option[Thread]]

    def add(boardName: String, thread: Thread): Future[LastError]
  }

}

trait ThreadRepositoryComponentImpl extends ThreadRepositoryComponent {

  override val threadRepository = new ThreadRepositoryImpl

  class ThreadRepositoryImpl extends ThreadRepository {
    protected val collectionName = "boards"
    protected val bsonDocumentHandler = Thread.threadBSONHandler

    def add(boardName: String, thread: Thread)
      = update(BSONDocument("name" -> boardName),
               BSONDocument("$push" -> BSONDocument("threads" -> thread)))

    // TODO: Perhaps abstract this out
    def findByBoardNameAndNo(boardName: String, no: Int) = {
      val command = Aggregate("boards", Seq(
        Unwind("threads"),
        Match(BSONDocument("name" -> boardName, "threads.op.no" -> no)),
        Project("threads" -> BSONInteger(1))
      ))

      (db.command(command) map { resultDocumentStream =>
        resultDocumentStream.toSeq map { resultDocument =>
          resultDocument.getAs[BSONDocument]("threads") map { threadDocument =>
            bsonDocumentHandler read threadDocument
          }
        }
      }) map (_.head)
    }
  }

}
