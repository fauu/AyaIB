package services

import scala.Option
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

import reactivemongo.bson.BSONValue
import com.github.t3hnar.bcrypt._

import repositories.StaffMemberRepositoryComponent
import models.entities.StaffMember

trait StaffServiceComponent {

  def staffService: StaffService

  trait StaffService {

    def authenticateMember(id: String, password: String): Future[Option[StaffMember]]

    def findMemberById(id: String): Future[Option[StaffMember]]

  }

}

trait StaffServiceComponentImpl extends StaffServiceComponent {
  this: StaffMemberRepositoryComponent =>

  def staffService = new StaffServiceImpl

  class StaffServiceImpl extends StaffService {

    def authenticateMember(id: String, password: String): Future[Option[StaffMember]] =
      staffMemberRepository.findOneById(id) map {
        case Some(member) if password isBcrypted member.password => Some(member)
        case _ => None
      }

    def findMemberById(id: String): Future[Option[StaffMember]] = staffMemberRepository.findOneById(id)

  }

}
