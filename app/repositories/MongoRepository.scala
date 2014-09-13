package repositories

import entities.MongoEntity
import reactivemongo.core.commands.{LastError, GetLastError}
import play.modules.reactivemongo.ReactiveMongoPlugin
import scala.concurrent.Future
import play.api.Play.current
import play.modules.reactivemongo.json.ImplicitBSONHandlers
import reactivemongo.api.collections.default.BSONCollection
import scala.concurrent.ExecutionContext.Implicits.global
import reactivemongo.bson._

trait MongoRepository extends ImplicitBSONHandlers {

  type A <: MongoEntity

  protected val db = ReactiveMongoPlugin.db
  protected val collectionName: String
  protected def collection = db[BSONCollection](collectionName)
  protected val awaitJournalCommit: GetLastError = GetLastError(j = true)
  implicit protected val bsonDocumentHandler: BSONDocumentReader[A] with BSONDocumentWriter[A]
                                                                    with BSONHandler[BSONDocument, A]

  def query(document: BSONDocument = BSONDocument(), filter: BSONDocument = BSONDocument()): Future[List[A]]
    = collection.find(document, filter).cursor[A].collect[List](1000, stopOnError = false)
  def queryOne(document: BSONDocument = BSONDocument(), filter: BSONDocument = BSONDocument()): Future[Option[A]]
    = collection.find(document, filter).one[A]
  def insert(a: A): Future[LastError] = collection.insert(a, awaitJournalCommit)
  def update(a: A): Future[LastError]
    = collection.update(BSONDocument("_id" -> a._id), a, awaitJournalCommit, upsert = false)
  def findOne(id: BSONObjectID): Future[Option[A]] = collection.find(BSONDocument("_id" -> id)).one[A]
  def findAll: Future[List[A]] = query()
  def exists(id: BSONObjectID): Future[Boolean] = findOne(id).map(_.isDefined)
  def remove(id: BSONObjectID) = collection.remove(BSONDocument("_id" -> id))

}
