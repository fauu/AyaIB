/*
 * Copyright (C) 2014 AyaIB Developers (http://github.com/fauu/AyaIB)
 *
 * This software is licensed under the GNU General Public License
 * (version 3 or later). See the COPYING file in this distribution.
 *
 * You should have received a copy of the GNU Library General Public License
 * along with this software. If not, see <http://www.gnu.org/licenses/>.
 *
 * Authored by: Piotr Grabowski <fau999@gmail.com>
 */

package repositories

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

import play.api.Play.current
import play.api.libs.json.{Format, Json, JsObject}
import play.modules.reactivemongo.ReactiveMongoPlugin
import play.modules.reactivemongo.json.ImplicitBSONHandlers
import play.modules.reactivemongo.json.collection.JSONCollection

import reactivemongo.core.commands.{Count, GetLastError, LastError}

import models.entities.MongoEntity
import reactivemongo.api.QueryOpts
import reactivemongo.bson.BSONDocument

trait MongoRepository extends ImplicitBSONHandlers {

  type A <: MongoEntity

  protected val db = ReactiveMongoPlugin.db
  protected val collectionName: String
  protected def collection = db[JSONCollection](collectionName)
  protected val awaitJournalCommit: GetLastError = GetLastError(j = true)
  implicit protected val jsonFormat: Format[A]

  def mongoSave(a: A): Future[LastError] = collection.insert(a, awaitJournalCommit)

  def mongoCount(selector: BSONDocument = BSONDocument()): Future[Int] =
    collection.db.command(Count(collection.name, Some(selector)))

  def mongoFind(selector: JsObject = Json.obj(), projection: JsObject = Json.obj()): Future[List[A]] =
    collection.find(selector, projection).cursor[A].collect[List](1000, stopOnError = false)

  def mongoFindSortedAndLimited(selector: JsObject = Json.obj(),
                                projection: JsObject = Json.obj(),
                                sort: JsObject,
                                start: Int,
                                count: Int): Future[List[A]] =
    collection.find(selector, projection)
              .options(QueryOpts(start, count))
              .sort(sort).cursor[A]
              .collect[List](1000, stopOnError = false)

  def mongoFindOne(selector: JsObject, projection: JsObject = Json.obj()): Future[Option[A]] =
    collection.find(selector, projection).one[A]

  def mongoUpdate(selector: JsObject, update: JsObject): Future[LastError] =
    collection.update(selector, update, awaitJournalCommit, upsert = false)

  def mongoRemove(selector: JsObject) = collection.remove(selector)

}
