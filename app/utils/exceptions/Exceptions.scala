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

package utils.exceptions

class ServiceException(msg: String = "Service exception") extends RuntimeException(msg)

class PersistenceException(msg: String = "Persistence exception") extends ServiceException(msg)

class IncorrectInputException(msg: String = "Bad user input exception") extends ServiceException(msg)
