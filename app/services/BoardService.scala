package services

import java.io.File

import scala.{Option, Some}
import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.util.{Failure, Success, Try}
import scala.util.matching.Regex.Match

import play.api.libs.concurrent.Execution.Implicits._

import com.sksamuel.scrimage.{AsyncImage, Format}
import models.entities._
import utils.exceptions._
import models.forms.PostForm
import reactivemongo.api.gridfs.DefaultFileToSave
import reactivemongo.bson.{BSONInteger, BSONObjectID}
import repositories._
import utils.Utils
import models.wrappers.FileWrapper
import com.github.nscala_time.time.Imports.DateTime
import java.util.regex.Pattern
import controllers.routes
import scala.xml.XML

trait BoardServiceComponent {

  def boardService: BoardService

  trait BoardService {

    def addPost(boardName: String,
                threadNoOption: Option[Int] = None,
                postData: PostForm,
                fileWrapperOption: Option[FileWrapper]): Future[Try[Int]]

    def findAllBoards: Future[List[models.entities.Board]]

    def findBoard(name: String): Future[Option[Board]]

    def findBoardLastPostNo(name: String): Future[Option[Int]]

    def findBoardWithThread(boardName: String, threadNo: Int): Future[Try[(Board, Thread)]]

    def findBoardWithThreadPage(boardName: String, pageNo: Int): Future[Try[(Board, List[Thread], Int)]]

  }

}

trait BoardServiceComponentImpl extends BoardServiceComponent {
  this: BoardRepositoryComponent
        with ThreadRepositoryComponent
        with PostIdRepositoryComponent
        with QuotationRepositoryComponent
        with FileRepositoryComponent =>

  def boardService = new BoardServiceImpl

  class BoardServiceImpl extends BoardService {

    private def thumbnailImage(image: AsyncImage): Future[AsyncImage] = {
      val thumbnailMaxDimension = 200

      if (image.dimensions._1 > thumbnailMaxDimension || image.dimensions._2 > thumbnailMaxDimension) {
        if (image.ratio >= 1) image.scaleToWidth(thumbnailMaxDimension)
        else image.scaleToHeight(thumbnailMaxDimension)
      } else Future(image)
    }

    // TODO: Add indication text to mark gif thumbnails
    // TODO: Make animated gif thumbnails
    private def processFile(fileWrapper: FileWrapper): Future[(FileWrapper, FileWrapper, FileMetadata)] = {
      fileWrapper.contentType getOrElse "" match {
        case contentType @ ("image/jpeg" | "image/png" | "image/gif") =>
          for {
            image <- AsyncImage(fileWrapper.file)

            imageCopy <- AsyncImage(fileWrapper.file)

            thumbnail <- thumbnailImage(imageCopy)

            // TODO: Stream this instead of using temp file?
            thumbnailFile = File.createTempFile("tmp", "ayafile")

            _ <- thumbnail.writer(Format.JPEG) write thumbnailFile

            thumbnailWrapper = new FileWrapper(file = thumbnailFile,
                                               filename = fileWrapper.filename,
                                               contentType = Some("image/jpeg"))
          } yield {
            (fileWrapper,
             thumbnailWrapper,
             FileMetadata(originalName = fileWrapper.filename,
                          dimensions = image.dimensions.productIterator map { _.toString } mkString "x",
                          size = Utils.humanizeFileLength(fileWrapper.file.length)))
          }
        case _ => throw new UnsupportedOperationException("File format not supported")
      }
    }

    private def fileValidity(boardConfig: BoardConfig, fileWrapper: FileWrapper): Future[Unit] =
      if (boardConfig.allowedContentTypes contains fileWrapper.contentType.getOrElse("")) Future.successful(Unit)
      else Future.failed(new IncorrectInputException("Corrupted file or forbidden file type"))

    private def generateFilename(fileWrapper: FileWrapper, timestamp: Long, thumbnail: Boolean = false): String = {
      val extension = Utils.contentTypeToExtension(fileWrapper.contentType.get).getOrElse("ayafile")
      val name = if (thumbnail) timestamp.toString + "_thumb" else timestamp.toString
      "%s.%s" format (name, extension)
    }

    private def processPostContent(board: Board, no: Int, content: String): String = {
      val quotationLinkPattern = """(?m)^(>>.*)$""".r
      val withQuotationLinks = quotationLinkPattern replaceAllIn (content, (m: Match) => {
        val matchedLinkMarkup = m.group(1)
        val matchedLinkPostNo = matchedLinkMarkup dropWhile (!_.isDigit)

        Utils.stringToInt(matchedLinkPostNo) match {
          case Some(linkPostNo) =>
            val futureFormattedLink = postIdRepository.findOne(board, linkPostNo) map {
              case Some(postId) =>
                """<a href="%s">%s</a>""" format (
                  """%s#post-%s""" format (routes.BoardController.showThread(board.name, postId.threadNo), linkPostNo),
                   matchedLinkMarkup
                )
              case _ => matchedLinkMarkup
            }
            quotationRepository.add(Quotation(_sourceBoard_id = board._id,
                                              sourceNo = no,
                                              _targetBoard_id = board._id,
                                              targetNo = linkPostNo))
            Await.result(futureFormattedLink, 5 seconds)
          case _ => matchedLinkMarkup
        }
      })

      val quotePattern = """(?m)^(>.*)$""".r
      val withQuotes = quotePattern replaceAllIn (withQuotationLinks, (m: Match) => {
          """<span class="quote">%s</span>""" format m.group(1)
      })

      withQuotes.replace("\n", "\n<br />")
    }

    def fixQuotations(targetBoard: Board, targetNo: Int): Future[Unit] = {
      val quotations = quotationRepository.findByTarget(targetBoard._id.get, targetNo)

      quotations map { futureQuotations =>
        futureQuotations map { quotation =>
          for {
            sourceBoardOption <- boardRepository findOne quotation._sourceBoard_id.get
            threadOption <- threadRepository findOneByBoardAndPostNo (sourceBoardOption.get, quotation.sourceNo)
            isOp = threadOption.get.op.no == quotation.sourceNo
            post = if (isOp) threadOption.get.op
                   else (threadOption.get.replies filter (_.no == quotation.sourceNo)).head
            fixedPost = post copy (
              content = processPostContent(board = sourceBoardOption.get,
                                           no = post.no,
                                           content = XML.loadString("<html>%s</html>" format post.content).text)
            )
            _ <- if (isOp) threadRepository updateOp (board = sourceBoardOption.get, threadNo = post.no, op = fixedPost)
                 else threadRepository updateReply (board = sourceBoardOption.get, replyNo = post.no, reply = fixedPost)
          } yield ()
        }
      }
    }

    def addPost(
      boardName: String,
      threadNoOption: Option[Int] = None,
      postData: PostForm,
      fileWrapperOption: Option[FileWrapper]
    ) = {
      val futureFileInfo = fileWrapperOption match {
        case Some(fileWrapper) =>
          for {
            Some(board) <- boardRepository findOneByName boardName // FIXME: DRY

            _ <- fileValidity(board.config, fileWrapper)

            (mainWrapper: FileWrapper, thumbWrapper: FileWrapper, fileMetadata: FileMetadata)
              <- processFile(fileWrapper)

            timestamp = System.currentTimeMillis()

            thumbFileToSave = DefaultFileToSave(filename = generateFilename(fileWrapper, timestamp, thumbnail = true),
                                                contentType = thumbWrapper.contentType)

            mainFileToSave = DefaultFileToSave(filename = generateFilename(fileWrapper, timestamp),
                                               contentType = fileWrapper.contentType)

            _ <- fileRepository add (thumbWrapper.file, thumbFileToSave, thumbnail = true)

            _ <- fileRepository add (fileWrapper.file, mainFileToSave)
          } yield (Some(mainFileToSave.filename), Some(fileMetadata), Some(thumbFileToSave.filename))
        case _ => Future.successful((None, None, None))
      }

      (for {
        Some(board) <- boardRepository findOneByName boardName // FIXME: DRY

        _ <- boardRepository.incrementLastPostNo(boardName)

        (fileNameOption: Option[String], fileMetadataOption: Option[FileMetadata], thumbnailNameOption: Option[String])
          <- futureFileInfo

        newPostNo = board.lastPostNo + 1

        newPost <- Future.successful {
          Post(no = newPostNo,
               subject = postData.subject,
               name = postData.name,
               email = postData.email,
               content = processPostContent(board, newPostNo, postData.content),
               date = DateTime.now,
               fileName = fileNameOption,
               fileMetadata = fileMetadataOption,
               thumbnailName = thumbnailNameOption)
        }

        _ <- threadNoOption match {
          case Some(threadNo) =>
             threadRepository.findOneByBoardAndNo(board, threadNo) flatMap { // TODO: This is excessive, fix
               case Some(thread) =>
                 (for {
                   lastError <- threadRepository.addReply(board, thread, newPost)
                   lastError <- threadRepository.incrementNumReplies(board, thread.no)
                   lastError <- postIdRepository.add(board, PostId(threadNo = threadNo, no = newPost.no))
                 } yield lastError) flatMap { lastError =>
                   if (newPost.email.getOrElse("") != "sage")
                     threadRepository.updateBumpDate(board, threadNo, newPost.date)
                   else
                     Future.successful(lastError)
                 }
               case _ => Future.failed(new PersistenceException("Cannot add reply: cannot retrieve thread"))
            }
          case _ =>
            for {
              lastError <- threadRepository.add(board, Thread(bumpDate = newPost.date, op = newPost))
              lastError <- postIdRepository.add(board, PostId(threadNo = newPost.no, no = newPost.no))
            } yield lastError
        }

        _ <- fixQuotations(targetBoard = board, targetNo = newPost.no)
      } yield {
        Success(newPost.no)
      }) recover {
        case (ex: IncorrectInputException) => Failure(ex)
        case (ex: Exception) => Failure(new PersistenceException(s"Cannot save post: $ex"))
      }
    }

    def findAllBoards = boardRepository.findAll

    def findBoard(name: String) = boardRepository.findOneByName(name)

    def findBoardLastPostNo(name: String) =
      boardRepository.findOneByName(name) map {
        case Some(board) => Some(board.lastPostNo)
        case _ => None
      }

    def findBoardWithThread(boardName: String, threadNo: Int) =
      boardRepository.findOneByName(boardName) flatMap {
        case Some(board) =>
          threadRepository.findOneByBoardAndNo(board, threadNo) map {
            case Some(thread) => Success((board, thread))
            case _ => Failure(new PersistenceException("Cannot retrieve thread"))
          }
        case _ => Future.successful(Failure(new PersistenceException("Cannot retrieve board")))
      }

    def findBoardWithThreadPage(boardName: String, pageNo: Int) = {
      boardRepository.findOneByName(boardName) flatMap {
        case Some(board) =>
          threadRepository.findCountByBoard(board) flatMap { threadCount =>
            val numPages = threadCount / board.config.threadsPerPage
            if ((1 to numPages) contains pageNo) {
              val futureThreads =
                threadRepository
                  .findExcerptsByBoardSortedByBumpDateDescLimited(board = board,
                    maxNumReplies = 3,
                    start = board.config.threadsPerPage * (pageNo - 1),
                    count = board.config.threadsPerPage)
              futureThreads map (threads => Success((board, threads, numPages)))
            } else Future.successful(Failure(new IncorrectInputException("Requested page does not exist")))
          }
        case _ => Future.successful(Failure(new PersistenceException("Cannot retrieve board")))
      }
    }

  }

}
