package services

import scala.concurrent.Future
import repositories.{FileRepositoryComponent, ThreadRepositoryComponent, BoardRepositoryComponent}
import entities._
import play.api.libs.concurrent.Execution.Implicits._
import reactivemongo.core.commands.LastError
import java.io.File
import reactivemongo.api.gridfs.{DefaultFileToSave, ReadFile, FileToSave}
import reactivemongo.bson.{BSONDocument, BSONValue, BSONObjectID}
import wrappers.FileWrapper
import scala.Option
import scala.util.Success
import forms.PostForm
import scala.Some
import reactivemongo.api.gridfs.DefaultFileToSave

trait BoardServiceComponent {

  def boardService: BoardService

  trait BoardService {
    def listBoards: Future[List[entities.Board]]
    def findBoardByName(name: String): Future[Option[Board]]
    def addThread(boardName: String, opPostData: PostForm, fileWrapper: FileWrapper): Future[Option[Int]]
    def findBoardLastPostNo(name: String): Future[Option[Int]]
  }

}

trait BoardServiceComponentImpl extends BoardServiceComponent {
  this: BoardRepositoryComponent
        with ThreadRepositoryComponent
        with FileRepositoryComponent =>

  def boardService = new BoardServiceImpl

  class BoardServiceImpl extends BoardService {
    def listBoards = boardRepository.findAllSimple

    def findBoardByName(name: String) = boardRepository.findByName(name)

    private def processFile(fileWrapper: FileWrapper): (FileWrapper, FileWrapper, FileMetadata) = {
      (fileWrapper, fileWrapper, FileMetadata(originalName = "testOrigName", dimensions = "testOrigDims"))
    }

    def addThread(boardName: String, opPostData: PostForm, fileWrapper: FileWrapper) = {
      ///
      //1. Check if contentType is allowed (get board config)
      //2. Get file dimensions (image OR later webm)
      //3. Get new filename (timestamped)
      //4. Create thumbnail
      //5. Save thumbnail
      //6. Save file
      //7. Add thread
      //8. Add op file ref

      (boardRepository findByNameSimple boardName) map { boardOption =>
        boardOption map { board =>
          if (board.config.allowedContentTypes contains fileWrapper.contentType) {
            processFile(fileWrapper) match {
              case (mainWrapper: FileWrapper, thumbWrapper: FileWrapper, metadata: FileMetadata) =>
                val fileMetadata = FileMetadata.fileMetadataBSONHandler write metadata

                val thumbFileToSave = DefaultFileToSave(filename = "TestThumbName",
                  contentType = thumbWrapper.contentType)
                val mainFileToSave = DefaultFileToSave(filename = "TestFilename",
                  contentType = fileWrapper.contentType,
                  metadata = fileMetadata)

                val thumbResultFuture = fileRepository.saveThumbnail(file = thumbWrapper.file,
                  fileToSave = thumbFileToSave)
                val mainResultFuture = fileRepository.save(file = fileWrapper.file, fileToSave = mainFileToSave)

                thumbResultFuture flatMap { fileResult =>
                  mainResultFuture flatMap { mainResult =>
                    boardRepository.incrementLastPostNo(boardName) flatMap { lastError =>
                      findBoardLastPostNo(boardName) map {
                        case Some(lastPostNo) =>
                          val newThread = Thread(_id = Some(BSONObjectID.generate),
                                                 op = Post(no = lastPostNo, content = opPostData.content),
                                                 replies = List[Post]())

                          threadRepository.add(boardName, newThread) flatMap { lastError =>
                            threadRepository.setOpFileRef(boardName, newThread._id, fileResult.id) map { lastError =>
                              Some(newThread.op.no)
                            }
                          }
                        case _ => Future(None)
                      } flatMap (x => x)
                    }
                  }
                } recover { case _ => None }
            }
          } else Future(None)
        } getOrElse Future(None)
      } flatMap (x => x)
    }

    def findBoardLastPostNo(name: String) =
      boardRepository.findByNameSimple(name) map {
        case Some(board) => Some(board.lastPostNo)
        case _ => None
      }
  }

}
