package repositories

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

import play.api.Play.current
import play.api.libs.json.{Format, Json, JsObject}
import play.modules.reactivemongo.ReactiveMongoPlugin
import play.modules.reactivemongo.json.ImplicitBSONHandlers
import play.modules.reactivemongo.json.collection.JSONCollection

import reactivemongo.core.commands.{GetLastError, LastError}

import models.entities.MongoEntity

trait MongoRepository extends ImplicitBSONHandlers {

  type A <: MongoEntity

  protected val db = ReactiveMongoPlugin.db
  protected val collectionName: String
  protected def collection = db[JSONCollection](collectionName)
  protected val awaitJournalCommit: GetLastError = GetLastError(j = true)
  implicit protected val jsonFormat: Format[A]

  def mongoSave(a: A): Future[LastError] = collection.insert(a, awaitJournalCommit)

  def mongoFind(selector: JsObject = Json.obj(), projection: JsObject = Json.obj()): Future[List[A]] =
    collection.find(selector, projection).cursor[A].collect[List](1000, stopOnError = false)

  def mongoFindSorted(selector: JsObject = Json.obj(),
                      projection: JsObject = Json.obj(),
                      sort: JsObject): Future[List[A]] =
    collection.find(selector, projection).sort(sort).cursor[A].collect[List](1000, stopOnError = false)

  def mongoFindOne(selector: JsObject, projection: JsObject = Json.obj()): Future[Option[A]] =
    collection.find(selector, projection).one[A]

  def mongoUpdate(selector: JsObject, update: JsObject): Future[LastError] =
    collection.update(selector, update, awaitJournalCommit, upsert = false)

  def mongoRemove(selector: JsObject) = collection.remove(selector)

}
