package entities

import reactivemongo.bson.BSONObjectID

trait MongoEntity extends Entity {

  def _id: Option[BSONObjectID]

}
