package repositories

import java.io.File

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

import play.api.libs.iteratee.Enumerator

import com.twitter.io.Files
import entities.FileMetadata
import reactivemongo.api.gridfs.{FileToSave, GridFS, ReadFile}
import reactivemongo.api.gridfs.Implicits._
import reactivemongo.bson._

trait FileRepositoryComponent {

  def fileRepository: FileRepository

  trait FileRepository extends MongoRepository {

    type A = FileMetadata

    def retrieveByName(name: String, thumbnail: Boolean): Future[Option[(ReadFile[BSONValue], Enumerator[Array[Byte]])]]

    def save(file: File, fileToSave: FileToSave[BSONValue]): Future[ReadFile[BSONValue]]

    def saveThumbnail(file: File, fileToSave: FileToSave[BSONValue]): Future[ReadFile[BSONValue]]

  }

}

trait FileRepositoryComponentImpl extends FileRepositoryComponent {

  override val fileRepository = new FileRepositoryImpl

  class FileRepositoryImpl extends FileRepository {

    protected val collectionName = "boards"
    protected val bsonDocumentHandler = FileMetadata.fileMetadataBSONHandler

    val gridFSMain = new GridFS(db, prefix = "main")
    val gridFSThumbnail = new GridFS(db, prefix = "thumb")

    def retrieveByName(name: String, thumbnail: Boolean) =
      (if (thumbnail) gridFSThumbnail else gridFSMain)
        .find[BSONDocument, ReadFile[BSONValue]](BSONDocument("filename" -> name))
        .headOption map (_.get) map {
          file => Some((file, (if (thumbnail) gridFSThumbnail else gridFSMain).enumerate(file)))
        } recover {
          case _ => None
        }


    def save(file: File, fileToSave: FileToSave[BSONValue]) =
      gridFSMain.save(Enumerator(Files.readBytes(file, limit = 1024 * 1024 * 15)), fileToSave)

    def saveThumbnail(file: File, fileToSave: FileToSave[BSONValue]) =
      gridFSThumbnail.save(Enumerator(Files.readBytes(file, limit = 1024 * 1024 * 15)), fileToSave)

  }

}
