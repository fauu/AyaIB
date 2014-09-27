package repositories

import scala.concurrent.Future

import play.api.libs.json.Json
import play.modules.reactivemongo.json.BSONFormats._

import reactivemongo.core.commands._
import reactivemongo.bson.BSONObjectID

import utils.json.FormatImplicits._
import models.entities.StaffMember

trait StaffMemberRepositoryComponent {

  def staffMemberRepository: StaffMemberRepository

  trait StaffMemberRepository extends MongoRepository {

    type A = StaffMember

    def add(staffMember: StaffMember): Future[LastError]

    def findOneById(id: String): Future[Option[StaffMember]]

  }

}

trait StaffMemberRepositoryComponentImpl extends StaffMemberRepositoryComponent {

  override val staffMemberRepository = new StaffMemberRepositoryImpl

  class StaffMemberRepositoryImpl extends StaffMemberRepository {

    protected val collectionName = "staffMembers"
    protected val jsonFormat = StaffMember.jsonFormat

    def add(staffMember: StaffMember) = mongoSave(staffMember)

    def findOneById(id: String) = mongoFindOne(Json.obj("id" -> id))

  }

}
