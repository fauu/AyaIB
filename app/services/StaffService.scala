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
