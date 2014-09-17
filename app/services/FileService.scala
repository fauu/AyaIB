package services

import scala.Option
import scala.concurrent.Future

import reactivemongo.api.gridfs.ReadFile
import repositories.FileRepositoryComponent
import play.api.libs.iteratee.Enumerator
import reactivemongo.bson.BSONValue

trait FileServiceComponent {

  def fileService: FileService

  trait FileService {
    def retrieveByName(name: String, thumbnail: Boolean): Future[Option[(ReadFile[BSONValue], Enumerator[Array[Byte]])]]
  }

}

trait FileServiceComponentImpl extends FileServiceComponent {
  this: FileRepositoryComponent =>

  def fileService = new FileServiceImpl

  class FileServiceImpl extends FileService {
    def retrieveByName(name: String, thumbnail: Boolean) = fileRepository.retrieveByName(name, thumbnail)
  }

}
