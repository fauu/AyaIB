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

package controllers

import play.api.mvc.{Action, Controller}

import scala.concurrent.ExecutionContext.Implicits.global

import context.AyaIBContext
import models.entities.Board

object ImageboardController extends Controller {

  val boardService = AyaIBContext.boardService

  def index = Action.async {
    boardService.findAllBoards map (boards => Ok(views.html.index(boards)))
  }

}