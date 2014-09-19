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

trait MongoRepository extends ImplicitBSONHandlers {

  type A <: MongoEntity

  protected val db = ReactiveMongoPlugin.db
  protected val collectionName: String
  protected def collection = db[BSONCollection](collectionName)
  protected val awaitJournalCommit: GetLastError = GetLastError(j = true)
  implicit protected val bsonDocumentHandler: BSONDocumentReader[A] with BSONDocumentWriter[A]
                                                                    with BSONHandler[BSONDocument, A]

  def query(selector: BSONDocument = BSONDocument(), filter: BSONDocument = BSONDocument()): Future[List[A]] =
    collection.find(selector, filter).cursor[A].collect[List](1000, stopOnError = false)

  def queryOne(selector: BSONDocument = BSONDocument(), filter: BSONDocument = BSONDocument()): Future[Option[A]] =
    collection.find(selector, filter).one[A]

  def insert(a: A): Future[LastError] = collection.insert(a, awaitJournalCommit)

  def update(selector: BSONDocument, modifier: BSONDocument): Future[LastError] =
    collection.update(selector, modifier, awaitJournalCommit, upsert = false)

  def upsert(selector: BSONDocument, modifier: BSONDocument): Future[LastError] =
    collection.update(selector, modifier, awaitJournalCommit, upsert = true)

  def findOne(id: BSONObjectID): Future[Option[A]] = collection.find(BSONDocument("_id" -> id)).one[A]

  def findAll: Future[List[A]] = query()

  def exists(id: BSONObjectID): Future[Boolean] = findOne(id).map(_.isDefined)

  def remove(id: BSONObjectID) = collection.remove(BSONDocument("_id" -> id))

}
