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

import play.api.libs.iteratee.Enumerator

import reactivemongo.api.gridfs.ReadFile
import reactivemongo.bson.BSONValue
import repositories.FileRepositoryComponent

trait FileServiceComponent {

  def fileService: FileService

  trait FileService {

    def findByName(name: String, thumbnail: Boolean): Future[Option[(ReadFile[BSONValue], Enumerator[Array[Byte]])]]

  }

}

trait FileServiceComponentImpl extends FileServiceComponent {
  this: FileRepositoryComponent =>

  def fileService = new FileServiceImpl

  class FileServiceImpl extends FileService {

    def findByName(name: String, thumbnail: Boolean) = fileRepository.findOneByName(name, thumbnail)

  }

}
