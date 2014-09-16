package repositories

import java.io.File

import scala.concurrent.Future

import com.twitter.io.Files
import entities.FileMetadata
import play.api.libs.iteratee.Enumerator
import reactivemongo.api.gridfs.Implicits.DefaultReadFileReader
import scala.concurrent.ExecutionContext.Implicits.global
import reactivemongo.api.gridfs.{ReadFile, FileToSave, GridFS}
import reactivemongo.bson._
import reactivemongo.core.commands.LastError

trait FileRepositoryComponent {

  def fileRepository: FileRepository

  trait FileRepository extends MongoRepository {
    type A = FileMetadata

    def save(file: File, fileToSave: FileToSave[BSONValue]): Future[ReadFile[BSONValue]]
    def saveThumbnail(file: File, fileToSave: FileToSave[BSONValue]): Future[ReadFile[BSONValue]]
  }

}

trait FileRepositoryComponentImpl extends FileRepositoryComponent {

  override val fileRepository = new FileRepositoryImpl

  class FileRepositoryImpl extends FileRepository {
    protected val collectionName = ""
    protected val bsonDocumentHandler = FileMetadata.fileMetadataBSONHandler

    val gridFSMain = new GridFS(db, prefix = "main")
    val gridFSThumbnail = new GridFS(db, prefix = "thumb")

    def save(file: File, fileToSave: FileToSave[BSONValue])
      = gridFSMain.save(Enumerator(Files.readBytes(file, limit = 1024 * 1024 * 15)), fileToSave)

    def saveThumbnail(file: File, fileToSave: FileToSave[BSONValue])
      = gridFSThumbnail.save(Enumerator(Files.readBytes(file, limit = 1024 * 1024 * 15)), fileToSave)
  }

}
