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
