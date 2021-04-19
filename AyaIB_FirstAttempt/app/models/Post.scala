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

package models

import models.Image.{TableName => ImageTableName}
import play.api.Play.current
import play.api.db.DB
import anorm._
import anorm.SqlParser._
import com.github.nscala_time.time.Imports._
import org.joda.time.format.DateTimeFormatter
import scala.Some
import anorm.MetaDataItem

case class Post(
    id: Pk[Long] = NotAssigned,
    no: Option[Long] = None,
    date: DateTime,
    subject: Option[String] = None,
    name: Option[String] = None,
    email: Option[String] = None,
    comment: String,
    isDeleted: Option[Boolean] = None,
    image: Option[Image] = None,
    boardId: Int,
    parentId: Option[Long] = None) {
  def hasImage = image.isDefined

  def isOp = parentId.isEmpty
}

case class PostFormData(name: String, email: String, subject: String, comment: String)

object Post {

  val TableName = "post"

  private val ColumnNamesWithImage =
    "p.id, p.no, p.date, p.subject, p.name, p.email, p.comment, p.isdeleted, p.image_name, p.board_id, " +
    "p.parent_id, i.origname, i.mimetype, i.size, i.width, i.height, i.isdeleted, i.hash"

  private val Store =
    """
    INSERT INTO {tableName} (no, date, subject, name, email, comment, image_name, board_id, parent_id)
    SELECT COALESCE(MAX(no) + 1, 1), {date}, {subject}, {name}, {email}, {comment}, {imageName}, {boardId},
           {parentId}
    FROM {tableName}
    WHERE board_id = {boardId}
    """.replace("{tableName}", TableName)

  private val LoadByBoardId =
    """
    SELECT {columnNamesWithImage}
    FROM {tableName} p
    LEFT JOIN {imageTableName} i
      ON p.image_name = i.name
    WHERE board_id = {boardId}
    """.replace("{tableName}", TableName).replace("{imageTable}", ImageTableName)
       .replace("{columnNamesWithImage}", ColumnNamesWithImage)

  private val LoadByBoardIdWithThreadLimit =
    """
    SELECT {columnNamesWithImage}
    FROM
      (SELECT COALESCE(parent_id, id) thread_id
       FROM
         {tableName}
       WHERE
         board_id = {boardId}
       GROUP BY thread_id
       ORDER BY MAX(id) DESC
       LIMIT {threadOffset}, {threadLimit}) px
    JOIN {tableName} p
      ON p.parent_id = px.thread_id OR p.id = px.thread_id
    LEFT JOIN {imageTableName} i
      ON p.image_name = i.name
    """.replace("{tableName}", TableName).replace("{imageTableName}", ImageTableName)
       .replace("{columnNamesWithImage}", ColumnNamesWithImage)

  private val LoadByBoardIdAndThreadNo =
    """
     SELECT {columnNamesWithImage}
     FROM {tableName} p
       LEFT JOIN {imageTableName} i ON p.image_name = i.name
     WHERE
       board_id = {boardId}
       AND (no = {threadNo} OR
           parent_id = (SELECT id FROM {tableName} WHERE no = {threadNo} AND board_id = {boardId}))
    """.replace("{tableName}", TableName).replace("{imageTableName}", ImageTableName)
       .replace("{columnNamesWithImage}", ColumnNamesWithImage)

  private val GetThreadIdByBoardIdAndThreadNo =
    "SELECT id FROM {tableName} WHERE board_id = {boardId} AND no = {threadNo}"
      .replace("{tableName}", TableName)

  private val GetThreadCountByBoardId =
    "SELECT COUNT(id) FROM {tableName} WHERE board_id = {boardId} AND parent_id IS NULL"
      .replace("{tableName}", TableName)

  def store(post: Post): Int = {
    DB.withConnection { implicit connection =>
      if (post.image.isDefined) {
        Image.store(post.image.get)
      }

      SQL(Store).on(
        'no -> post.no,
        'date -> new java.sql.Timestamp(post.date.getMillis),
        'subject -> (if (post.subject.get.length > 0) post.subject else None),
        'name -> (if (post.name.get.length > 0) post.name else None),
        'email -> (if (post.email.get.length > 0) post.email else None),
        'comment -> post.comment,
        'isDeleted -> post.isDeleted,
        'imageName -> (if (post.image.isDefined) post.image.get.name.get else None),
        'boardId -> post.boardId,
        'parentId -> post.parentId
      ).executeUpdate
    }
  }

  // TODO: Get this out of here
  val dateFormatGeneration: DateTimeFormatter = DateTimeFormat.forPattern("yyyyMMddHHmmssSS")

  implicit def rowToDateTime: Column[DateTime] = Column.nonNull { (value, meta) =>
    val MetaDataItem(qualified, nullable, clazz) = meta

    value match {
        case ts: java.sql.Timestamp => Right(new DateTime(ts.getTime))
        case d: java.sql.Date => Right(new DateTime(d.getTime))
        case str: java.lang.String => Right(dateFormatGeneration.parseDateTime(str))
        case _ => Left(TypeDoesNotMatch("Cannot convert " + value + ":" + value.asInstanceOf[AnyRef].getClass))
    }
  }

  private val postWithImageParser: RowParser[Post] = {
    get[Pk[Long]]("id") ~
    get[Option[Long]]("no") ~
    get[DateTime]("date") ~
    get[Option[String]]("subject") ~
    get[Option[String]]("name") ~
    get[Option[String]]("email") ~
    get[String]("comment") ~
    get[Option[Boolean]]("isdeleted") ~
    get[Option[String]]("image_name") ~
    get[Int]("board_id") ~
    get[Option[Long]]("parent_id") ~
    get[Option[String]]("origname") ~
    get[Option[String]]("mimetype") ~
    get[Option[String]]("size") ~
    get[Option[Int]]("width") ~
    get[Option[Int]]("height") ~
    get[Option[Boolean]]("isdeleted") ~
    get[Option[String]]("hash") map {
      case id ~ no ~ date ~ subject ~ name ~ email ~ comment ~ isDeleted ~ imageName ~ boardId ~ parentId ~
           imageOriginalName ~ imageMimeType ~ imageSize ~ imageWidth ~ imageHeight ~ imageIsDeleted ~
           imageHash =>
        val image: Option[Image] = imageName match {
          case Some(imageName) =>
            Some(Image(
              Id(imageName),
              imageOriginalName.get,
              imageMimeType.get,
              imageSize.get,
              imageWidth.get,
              imageHeight.get,
              imageIsDeleted,
              imageHash.get))
          case None => None
        }

        Post(id, no, date, subject, name, email, comment, isDeleted, image, boardId, parentId)
    }
  }

  def loadByBoardId(id: Int): List[Post] = {
    DB.withConnection { implicit connection =>
      SQL(LoadByBoardId).on('boardId -> id).as(postWithImageParser *).toList
    }
  }

  def loadByBoardIdWithThreadLimit(id: Int, threadOffset: Int, threadLimit: Int): List[Post] = {
    DB.withConnection { implicit connection =>
      SQL(LoadByBoardIdWithThreadLimit)
        .on('boardId -> id, 'threadOffset -> threadOffset, 'threadLimit -> threadLimit)
        .as(postWithImageParser *).toList
    }
  }

  def loadByBoardIdAndThreadNo(id: Int, no: Long): List[Post] = {
    DB.withConnection { implicit connection =>
      SQL(LoadByBoardIdAndThreadNo).on('boardId -> id, 'threadNo -> no).as(postWithImageParser *).toList
    }
  }

  def getThreadIdByBoardIdAndThreadNo(id: Int, no: Long): Long = {
    DB.withConnection { implicit connection =>
      SQL(GetThreadIdByBoardIdAndThreadNo)
        .on('boardId -> id, 'threadNo -> no)
        .as(scalar[Long].single)
    }
  }

  def getThreadCountByBoardId(id: Int): Int = {
    DB.withConnection { implicit connection =>
      SQL(GetThreadCountByBoardId).on('boardId -> id).as(scalar[Long].single).toInt
    }
  }

}
