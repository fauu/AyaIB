package entities

import reactivemongo.bson.BSONObjectID

trait MongoEntity {

  val _id: Option[BSONObjectID]

}
