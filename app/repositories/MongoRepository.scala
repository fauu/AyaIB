package repositories

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

import play.api.Play.current
import play.modules.reactivemongo.ReactiveMongoPlugin
import play.modules.reactivemongo.json.ImplicitBSONHandlers

import entities.MongoEntity
import reactivemongo.api.collections.default.BSONCollection
import reactivemongo.bson._
import reactivemongo.core.commands.{GetLastError, LastError}
import com.github.nscala_time.time.Imports.DateTime

trait MongoRepository extends ImplicitBSONHandlers {

  type A <: MongoEntity

  protected val db = ReactiveMongoPlugin.db
  protected val collectionName: String
  protected def collection = db[BSONCollection](collectionName)
  protected val awaitJournalCommit: GetLastError = GetLastError(j = true)
  implicit protected val bsonDocumentHandler: BSONDocumentReader[A] with BSONDocumentWriter[A]
                                                                    with BSONHandler[BSONDocument, A]

  implicit object DateTimeReader extends BSONReader[BSONDateTime, DateTime] {
    def read(bson: BSONDateTime): DateTime = new DateTime(bson.value)
  }

  implicit object DateTimeWriter extends BSONWriter[DateTime, BSONDateTime] {
    def write(t: DateTime): BSONDateTime = BSONDateTime(t.getMillis)
  }

  def mongoSave(a: A): Future[LastError] = collection.insert(a, awaitJournalCommit)

  def mongoFind(selector: BSONDocument = BSONDocument(), filter: BSONDocument = BSONDocument()): Future[List[A]] =
    collection.find(selector, filter).cursor[A].collect[List](1000, stopOnError = false)

  def mongoFindOne(selector: BSONDocument = BSONDocument(), filter: BSONDocument = BSONDocument()): Future[Option[A]] =
    collection.find(selector, filter).one[A]

  def mongoUpdate(selector: BSONDocument, modifier: BSONDocument): Future[LastError] =
    collection.update(selector, modifier, awaitJournalCommit, upsert = false)

  def mongoRemove(id: BSONObjectID) = collection.remove(BSONDocument("_id" -> id))

}
