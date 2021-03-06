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

package repositories

import java.io.File

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

import play.api.libs.iteratee.Enumerator

import utils.json.FormatImplicits._
import com.twitter.io.Files
import models.entities.FileMetadata
import reactivemongo.api.gridfs.{FileToSave, GridFS, ReadFile}
import reactivemongo.api.gridfs.Implicits._
import reactivemongo.bson._
import play.api.libs.json.Json
import play.api.Play.current
import play.modules.reactivemongo.ReactiveMongoPlugin

trait FileRepositoryComponent {

  def fileRepository: FileRepository

  trait FileRepository {

    type A = FileMetadata

    def add(file: File, fileToSave: FileToSave[BSONValue], thumbnail: Boolean = false): Future[ReadFile[BSONValue]]

    def findOneByName(name: String, thumbnail: Boolean): Future[Option[(ReadFile[BSONValue], Enumerator[Array[Byte]])]]

  }

}

trait FileRepositoryComponentImpl extends FileRepositoryComponent {

  override val fileRepository = new FileRepositoryImpl

  class FileRepositoryImpl extends FileRepository {

    protected val db = ReactiveMongoPlugin.db

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
