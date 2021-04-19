/*
 * Copyright (C) 2014 AyaIB Developers (http://github.com/fauu/ayaib)
 *
 * This software is licensed under the GNU General Public License
 * (version 3 or later). See the COPYING file in this distribution.
 *
 * You should have received a copy of the GNU Library General Public License
 * along with this software. If not, see <http://www.gnu.org/licenses/>.
 *
 * Authored by: Piotr Grabowski <fau999@gmail.com>
 */

package controllers

import models.{Board, Image, Post, PostFormData}
import play.api.Play
import play.api.data._
import play.api.data.Forms._
import play.api.libs.Files
import play.api.mvc._
import anorm.{Id, NotAssigned}
import scala.Some
import java.awt.image.BufferedImage
import java.io.{ByteArrayOutputStream, File}
import java.security.MessageDigest
import java.util.Random
import collection.JavaConversions._
import com.github.nscala_time.time.Imports._
import javax.imageio.ImageIO
import utils.Utils._

object Boards extends Controller {

  def viewThread(boardUri: String, threadNo: Long) = Action {
    view(boardUri, Some(threadNo), None)
  }

  def viewBoard(boardUri: String, page: Int) = Action {
    view(boardUri, None, Some(page))
  }

  private def view(boardUri: String, threadNo: Option[Long], page: Option[Int]): Result = {
    val boards = Board.loadAll.sortBy(b => b.uri)

    boards.find(_.uri == boardUri).fold(NotFound(views.html.notFound())) { currentBoard =>
      val isThreadView = threadNo.isDefined

      if (isThreadView) {
        val posts = Post.loadByBoardIdAndThreadNo(currentBoard.id.get, threadNo.get)

        if (posts.isEmpty || !posts.head.isOp) NotFound(views.html.notFound())
        else Ok(views.html.board(
                  allBoards = boards,
                  board = currentBoard,
                  postForm,
                  threads = List(posts),
                  isThreadView,
                  numPages = None,
                  currentPage = None))
      } else {
        val threadsPerPage = Play.current.configuration.getInt("ayaib.threadsPerPage").get

        val numPages = (Post.getThreadCountByBoardId(currentBoard.id.get) / threadsPerPage.toFloat).ceil.toInt
          match {
            case 0 => 1
            case n => n
          }

        if (page.get > numPages - 1) NotFound(views.html.notFound())
        else {
          val threads: List[List[Post]] =
            Post.loadByBoardIdWithThreadLimit(currentBoard.id.get, threadsPerPage * page.get, threadsPerPage)
                .groupBy(post => post.parentId.getOrElse(post.id.get))
                .map(_._2).toList // Ignore grouping key (parentId)
                .sortBy(- _.last.id.get) // Sort threads by last post id descending

          Ok(views.html.board(
               allBoards = boards,
               board = currentBoard,
               postForm,
               threads,
               isThreadView,
               Some(numPages),
               currentPage = page))
        }
      }
    }
  }

  val postForm = Form(
    mapping(
      "name" -> text,
      "email" -> text,
      "subject" -> text,
      "comment" -> text(minLength = 1, maxLength = 2000)
    )(PostFormData.apply)(PostFormData.unapply)
  )

  def postInThread(boardUri: String, threadNo: Long) = Action(parse.multipartFormData) { implicit request =>
    post(boardUri, Some(threadNo), request.body.file("image"))
  }

  def postNewThread(boardUri: String) = Action(parse.multipartFormData) { implicit request =>
    post(boardUri, None, request.body.file("image"))
  }

  private def post(boardUri: String,
                   threadNo: Option[Long],
                   imageFileOption: Option[MultipartFormData.FilePart[Files.TemporaryFile]])
                  (implicit request: Request[_]): Result = {

    Board.loadByUri(boardUri).fold(NotFound(views.html.notFound())) { board =>
      val image: Option[Image] = imageFileOption match {
        case Some(imageFile) =>
          val filename = imageFile.filename
          val contentType = imageFile.contentType.get
          val acceptedContentTypes =
            Play.current.configuration.getStringList("ayaib.acceptedContentTypes").map(_.toList)
                .getOrElse(List("image/jpg", "image/jpeg"))

          val random = new Random().nextInt(100)
          // Appends random 2-digit number with leading zeros
          val newFilenameMain = DateTime.now.getMillis.toString + ("00" + random takeRight 2)

          if (!acceptedContentTypes.contains(contentType)) None
          else {
            val uploadDir = Play.current.configuration.getString("ayaib.uploadDir").get
            val newFilenameExtension = contentTypeToExtension(contentType)
            val newFilenameWithPath = uploadDir + newFilenameMain + "." + newFilenameExtension

            println(newFilenameWithPath)
            imageFile.ref.moveTo(new File(newFilenameWithPath))

            val imageFile2 = new File(newFilenameWithPath)
            val imageSize = humanizeFileSize(imageFile2.length)
            val bufferedImage = ImageIO.read(imageFile2)
            val imageDimensions = (bufferedImage.getWidth, bufferedImage.getHeight)

            val thumbnailDimensions = scaleDimensions(imageDimensions, (200, 200))
            val thumbnailFullPath = uploadDir + newFilenameMain + "t." + newFilenameExtension

            val scaledImage =
              bufferedImage.getScaledInstance(
                thumbnailDimensions._1,
                thumbnailDimensions._2,
                java.awt.Image.SCALE_SMOOTH)

            val thumbnail =
              new BufferedImage(
                thumbnailDimensions._1,
                thumbnailDimensions._2,
                BufferedImage.TYPE_INT_RGB)

            thumbnail.createGraphics().drawImage(scaledImage, 0, 0, null)
            ImageIO.write(thumbnail, newFilenameExtension, new File(thumbnailFullPath))

            val outputStream = new ByteArrayOutputStream

            ImageIO.write(bufferedImage, newFilenameExtension, outputStream)

            val byteArray = outputStream.toByteArray
            val md = MessageDigest.getInstance("MD5")
            val imageHash = bytesToHex(md.digest(byteArray))

            Some(Image(
              name = Id(newFilenameMain),
              originalName = filename,
              mimeType = contentType,
              size = imageSize,
              width = imageDimensions._1,
              height = imageDimensions._2,
              isDeleted = Option.empty,
              hash = imageHash))
          }
        case None => None
      }

      postForm.bindFromRequest()(request).fold(
        hasErrors = _ => {
          BadRequest
        },
        success = postData => {
          if (threadNo.isEmpty && image.isEmpty) BadRequest
          else {
            val parentId: Option[Long] = threadNo match {
              case Some(threadNo) => Some(Post.getThreadIdByBoardIdAndThreadNo(board.id.get, threadNo))
              case None => None
            }

            val newPost =
              models.Post(
                id = NotAssigned,
                no = Option.empty,
                date = DateTime.now,
                subject = Option(postData.subject),
                name = Option(postData.name),
                email = Option(postData.email),
                comment = postData.comment,
                isDeleted = Option.empty,
                image,
                boardId = board.id.get,
                parentId)

            Post.store(newPost)

            threadNo match {
              case Some(threadNo) => Redirect(routes.Boards.viewThread(boardUri, threadNo))
              case None => Redirect(routes.Boards.viewBoard(boardUri, 0))
            }
          }
        }
      )
    }
  }

}
