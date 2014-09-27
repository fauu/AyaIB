package auth

sealed trait Permission {

  val level: Int

}

object Permission {

  def valueOf(value: String): Permission = value match {
    case "administrator" => Administrator
    case "moderator" => Moderator
    case "janitor" => Janitor
    case _ => throw new IllegalArgumentException()
  }

}

case object Administrator extends Permission {

  val level = 3

  override def toString = "administrator"

}

case object Moderator extends Permission {

  val level = 2

  override def toString = "moderator"

}

case object Janitor extends Permission {

  val level = 1

  override def toString = "janitor"

}


