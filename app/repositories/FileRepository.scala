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

    def add(file: File, fileToSave: FileToSave[BSONValue], thumbnail: Boolean = false): Future[ReadFile[BSONValue]]

    def findOneByName(name: String, thumbnail: Boolean): Future[Option[(ReadFile[BSONValue], Enumerator[Array[Byte]])]]

  }

}

trait FileRepositoryComponentImpl extends FileRepositoryComponent {

  override val fileRepository = new FileRepositoryImpl

  class FileRepositoryImpl extends FileRepository {

    protected val collectionName = "boards"
    protected val bsonDocumentHandler = FileMetadata.fileMetadataBSONHandler

    protected val gridFSFiles = new GridFS(db, prefix = "files")
    protected val gridFSThumbnails = new GridFS(db, prefix = "thumbs")

    protected val MaxFileSizeBytes = 1024 * 1024 * 15

    def add(file: File, fileToSave: FileToSave[BSONValue], thumbnail: Boolean = false) = {
      if (thumbnail) gridFSThumbnails.save(Enumerator(Files.readBytes(file, limit = MaxFileSizeBytes)), fileToSave)
      else gridFSFiles.save(Enumerator(Files.readBytes(file, limit = MaxFileSizeBytes)), fileToSave)
    }

    def findOneByName(name: String, thumbnail: Boolean) =
      (if (thumbnail) gridFSThumbnails else gridFSFiles)
        .find[BSONDocument, ReadFile[BSONValue]](BSONDocument("filename" -> name))
        .headOption map (_.get) map {
          file => Some((file, (if (thumbnail) gridFSThumbnails else gridFSFiles).enumerate(file)))
        } recover {
          case _ => None
        }

  }

}
