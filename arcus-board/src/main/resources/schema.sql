CREATE TABLE IF NOT EXISTS `user` (
  `uid` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(45) NOT NULL,
  `created_date` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`uid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


CREATE TABLE IF NOT EXISTS `board` (
  `bid` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(45) NOT NULL,
  `category` int(11) NOT NULL,
  `req_today` bigint(20) unsigned NOT NULL DEFAULT '0',
  PRIMARY KEY (`bid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


CREATE TABLE IF NOT EXISTS `post` (
  `pid` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `uid` bigint(20) unsigned NOT NULL,
  `bid` bigint(20) unsigned NOT NULL,
  `category` int(11) NOT NULL,
  `title` varchar(256) NOT NULL,
  `content` text NOT NULL,
  `views` bigint(20) unsigned NOT NULL DEFAULT '0',
  `likes` bigint(20) unsigned NOT NULL DEFAULT '0',
  `created_date` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_date` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `cmtCnt` bigint(20) unsigned NOT NULL DEFAULT '0',
  PRIMARY KEY (`pid`),
  KEY `foreign_uid` (`uid`),
  KEY `idx_post` (`bid`,`pid`),
  KEY `idx_category` (`bid`,`category`,`pid`),
  KEY `idx_likes` (`bid`,`created_date`,`likes`),
  KEY `idx_views` (`bid`,`created_date`,`views`),
  CONSTRAINT `post_board_bid` FOREIGN KEY (`bid`) REFERENCES `board` (`bid`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `post_user_uid` FOREIGN KEY (`uid`) REFERENCES `user` (`uid`) ON DELETE NO ACTION ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


CREATE TABLE IF NOT EXISTS `comment` (
  `cid` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `uid` bigint(20) unsigned NOT NULL,
  `pid` bigint(20) unsigned NOT NULL,
  `content` text NOT NULL,
  `created_date` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_date` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`cid`),
  KEY `foreign_uid` (`uid`),
  KEY `idx_post` (`pid`,`cid`),
  CONSTRAINT `comment_post_bid` FOREIGN KEY (`pid`) REFERENCES `post` (`pid`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `comment_user_uid` FOREIGN KEY (`uid`) REFERENCES `user` (`uid`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


CREATE TABLE IF NOT EXISTS `category_board` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(45) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


CREATE TABLE IF NOT EXISTS `category_post` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(45) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


CREATE TABLE IF NOT EXISTS `best_likes_all` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `period` int(11) NOT NULL,
  `rank` int(11) unsigned NOT NULL,
  `pid` bigint(20) unsigned NOT NULL,
  PRIMARY KEY (`id`),
  KEY `foreign_pid` (`pid`),
  KEY `idx_ranking` (`period`,`rank`),
  CONSTRAINT `best_likes_all_pid` FOREIGN KEY (`pid`) REFERENCES `post` (`pid`) ON DELETE NO ACTION ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


CREATE TABLE IF NOT EXISTS `best_likes_board` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `bid` bigint(20) unsigned NOT NULL,
  `period` int(11) NOT NULL,
  `rank` int(11) unsigned NOT NULL,
  `pid` bigint(20) unsigned NOT NULL,
  PRIMARY KEY (`id`),
  KEY `foreign_pid` (`pid`),
  KEY `idx_ranking` (`bid`,`period`,`rank`),
  CONSTRAINT `best_likes_board_bid` FOREIGN KEY (`bid`) REFERENCES `board` (`bid`) ON DELETE CASCADE ON UPDATE NO ACTION,
  CONSTRAINT `best_likes_board_pid` FOREIGN KEY (`pid`) REFERENCES `post` (`pid`) ON DELETE NO ACTION ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


CREATE TABLE IF NOT EXISTS `best_views_all` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `period` int(11) NOT NULL,
  `rank` int(11) unsigned NOT NULL,
  `pid` bigint(20) unsigned NOT NULL,
  PRIMARY KEY (`id`),
  KEY `foreign_pid` (`pid`),
  KEY `idx_ranking` (`period`,`rank`),
  CONSTRAINT `best_views_all_pid` FOREIGN KEY (`pid`) REFERENCES `post` (`pid`) ON DELETE NO ACTION ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


CREATE TABLE IF NOT EXISTS `best_views_board` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `bid` bigint(20) unsigned NOT NULL,
  `period` int(11) NOT NULL,
  `rank` int(11) unsigned NOT NULL,
  `pid` bigint(20) unsigned NOT NULL,
  PRIMARY KEY (`id`),
  KEY `foreign_pid` (`pid`),
  KEY `idx_ranking` (`bid`,`period`,`rank`),
  CONSTRAINT `best_views_board_bid` FOREIGN KEY (`bid`) REFERENCES `board` (`bid`) ON DELETE CASCADE ON UPDATE NO ACTION,
  CONSTRAINT `best_views_board_pid` FOREIGN KEY (`pid`) REFERENCES `post` (`pid`) ON DELETE NO ACTION ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


CREATE TABLE IF NOT EXISTS `best_board_request` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `bid` bigint(20) unsigned NOT NULL,
  `time` time NOT NULL,
  `request` bigint(20) unsigned NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_time` (`bid`,`time`),
  CONSTRAINT `board_bid` FOREIGN KEY (`bid`) REFERENCES `board` (`bid`) ON DELETE CASCADE ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;