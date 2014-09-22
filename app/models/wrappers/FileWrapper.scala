package models.wrappers

import java.io.File

class FileWrapper(val file: File, val filename: String, val contentType: Option[String]) { }
