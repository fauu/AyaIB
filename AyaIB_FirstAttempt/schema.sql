--
-- Table structure for table `board`
--

DROP TABLE IF EXISTS `board`;
CREATE TABLE `board` (
  `id` tinyint(5) NOT NULL AUTO_INCREMENT,
  `uri` varchar(20) NOT NULL,
  `title` varchar(100) NOT NULL,
  `subtitle` tinytext,
  `pages` tinyint(2) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uri_UNIQUE` (`uri`),
  UNIQUE KEY `id_UNIQUE` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8;

--
-- Table structure for table `image`
--

DROP TABLE IF EXISTS `image`;
CREATE TABLE `image` (
  `name` varchar(15) NOT NULL,
  `origname` text NOT NULL,
  `mimetype` varchar(20) NOT NULL,
  `size` varchar(10) NOT NULL,
  `width` int(5) NOT NULL,
  `height` int(5) NOT NULL,
  `isdeleted` tinyint(1) NOT NULL DEFAULT '0',
  `hash` varchar(32) NOT NULL,
  PRIMARY KEY (`name`),
  UNIQUE KEY `name_UNIQUE` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table structure for table `post`
--

DROP TABLE IF EXISTS `post`;
CREATE TABLE `post` (
  `id` int(15) NOT NULL AUTO_INCREMENT,
  `no` int(15) NOT NULL,
  `date` datetime NOT NULL,
  `subject` varchar(100) DEFAULT NULL,
  `name` varchar(60) DEFAULT NULL,
  `email` tinytext,
  `comment` text NOT NULL,
  `isdeleted` tinyint(1) NOT NULL DEFAULT '0',
  `image_name` varchar(15) DEFAULT NULL,
  `board_id` tinyint(5) NOT NULL,
  `parent_id` int(15) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`),
  KEY `fk_post_image1_idx` (`image_name`),
  KEY `fk_post_board1_idx` (`board_id`),
  KEY `fk_post_post1_idx` (`parent_id`),
  CONSTRAINT `fk_post_board1` FOREIGN KEY (`board_id`) REFERENCES `board` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `fk_post_image1` FOREIGN KEY (`image_name`) REFERENCES `image` (`name`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `fk_post_post1` FOREIGN KEY (`parent_id`) REFERENCES `post` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8;
