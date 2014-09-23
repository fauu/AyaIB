package repositories

import scala.concurrent.Future

import play.api.libs.json.Json
import play.modules.reactivemongo.json.BSONFormats._

import reactivemongo.core.commands._
import reactivemongo.bson.BSONObjectID

import com.github.nscala_time.time.Imports.DateTime

import utils.json.FormatImplicits._
import models.entities.Quotation

trait QuotationRepositoryComponent {

  def quotationRepository: QuotationRepository

  trait QuotationRepository extends MongoRepository {

    type A = Quotation

    def add(quotation: Quotation): Future[LastError]

    def findByTarget(_targetBoard_id: BSONObjectID, targetNo: Int): Future[List[Quotation]]

  }

}

trait QuotationRepositoryComponentImpl extends QuotationRepositoryComponent {

  override val quotationRepository = new QuotationRepositoryImpl

  class QuotationRepositoryImpl extends QuotationRepository {

    protected val collectionName = "quotations"
    protected val jsonFormat = Quotation.jsonFormat

    def add(quotation: Quotation) = mongoSave(quotation)

    def findByTarget(_targetBoard_id: BSONObjectID, targetNo: Int) =
      mongoFind(Json.obj("_targetBoard_id" -> _targetBoard_id, "targetNo" -> targetNo))

  }

}
