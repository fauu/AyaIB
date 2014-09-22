package controllers

import scala.concurrent.ExecutionContext.Implicits._

import play.api.mvc.{Action, Controller, ResponseHeader, Result}

import context.AyaIBContext

object FileController extends Controller {

  val fileService = AyaIBContext.fileService

  def get(name: String, thumbnail: Boolean) = Action.async {
    fileService findByName (name, thumbnail) map {
      case Some((file, enumerator)) =>
        Result(
          header = ResponseHeader(OK, Map(
            CONTENT_LENGTH -> file.length.toString,
            CONTENT_DISPOSITION -> (s"""inline; filename="${file.filename}"; filename*=UTF-8''"""  +
                                    java.net.URLEncoder.encode(file.filename, "UTF-8").replace("+", "%20")),
            CONTENT_TYPE -> file.contentType.getOrElse("application/octet-stream"))),
          body = enumerator
        )
      case _ => NotFound(views.html.notFound())
    }
  }

}
