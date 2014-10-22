/*
 * Copyright (C) 2014 AyaIB Developers (http://github.com/fauu/AyaIB)
 *
 * This software is licensed under the GNU General Public License
 * (version 3 or later). See the COPYING file in this distribution.
 *
 * You should have received a copy of the GNU Library General Public License
 * along with this software. If not, see <http://www.gnu.org/licenses/>.
 *
 * Authored by: Piotr Grabowski <fau999@gmail.com>
 */

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


