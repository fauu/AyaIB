package controllers

import context.Context
import scala.concurrent.ExecutionContext.Implicits._
import play.api.mvc.{ResponseHeader, Result, Action, Controller}

object FileController extends Controller {

  val fileService = Context.fileService

  def get(name: String, thumbnail: Boolean) = Action.async {
    fileService.retrieveByName(name, thumbnail) map {
      case Some((file, enumerator)) =>
        Result(
          header = ResponseHeader(OK, Map(
            CONTENT_LENGTH -> file.length.toString,
            CONTENT_DISPOSITION -> (s"""inline; filename="${file.filename}"; filename*=UTF-8''"""
                                    + java.net.URLEncoder.encode(file.filename, "UTF-8").replace("+", "%20")),
            CONTENT_TYPE -> file.contentType.getOrElse("application/octet-stream"))),
          body = enumerator
        )
      case _ => NotFound(views.html.notFound())
    }
  }

}
