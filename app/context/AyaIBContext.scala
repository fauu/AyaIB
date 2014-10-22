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

package context

import repositories._
import services._

object AyaIBContext {

  val boardServiceComponent = new BoardServiceComponentImpl with BoardRepositoryComponentImpl
                                                            with ThreadRepositoryComponentImpl
                                                            with PostIdRepositoryComponentImpl
                                                            with QuotationRepositoryComponentImpl
                                                            with FileRepositoryComponentImpl

  val boardService = boardServiceComponent.boardService

  val fileServiceComponent = new FileServiceComponentImpl with FileRepositoryComponentImpl

  val fileService = fileServiceComponent.fileService

  val staffServiceComponent = new StaffServiceComponentImpl with StaffMemberRepositoryComponentImpl

  val staffService = staffServiceComponent.staffService

}
