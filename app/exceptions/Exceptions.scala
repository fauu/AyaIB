package exceptions

class ServiceException(msg: String = "Service exception") extends RuntimeException(msg)

class PersistenceException(msg: String = "Persistence exception") extends ServiceException(msg)

class BadInputException(msg: String = "Bad user input exception") extends ServiceException(msg)
