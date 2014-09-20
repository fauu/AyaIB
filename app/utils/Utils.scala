package utils

import java.text.DecimalFormat
import com.github.nscala_time.time.Imports.{DateTime, DateTimeFormat}

object Utils {

  private lazy val fileLengthConversionThresholds =
    List((1073741824, "GB", "#.##"),
         (1048576, "MB", "#.##"),
         (1024, "KB", "#"),
         (1, "bytes", "#"))

  private lazy val contentTypesExtensions =
     Map("image/jpeg" -> "jpg",
         "image/png" -> "png",
         "image/gif" -> "gif")

  def humanizeFileLength(length: Long): String =
    (fileLengthConversionThresholds dropWhile (_._1 > length) take 1 map { threshold =>
      (new DecimalFormat(threshold._3) format (length.toFloat / threshold._1)) + " " + threshold._2
    }).head

  def contentTypeToExtension(contentType: String): Option[String] = contentTypesExtensions.get(contentType)

  def formatDate(date: DateTime, pattern: String = "yyyy-MM-dd H:mm:ss") =
    DateTimeFormat.forPattern(pattern) print date

}
