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
  }

}

trait FileRepositoryComponentImpl extends FileRepositoryComponent {

  override val fileRepository = new FileRepositoryImpl

  class FileRepositoryImpl extends FileRepository {
    protected val collectionName = "fs.files"
    protected val bsonDocumentHandler = FileMetadata.fileMetadataBSONHandler

    val gridFS = new GridFS(db)

    def save(file: File, fileToSave: FileToSave[BSONValue])
      = gridFS.save(Enumerator(Files.readBytes(file = file, limit = 1024 * 1024 * 15)), fileToSave)
  }

}
