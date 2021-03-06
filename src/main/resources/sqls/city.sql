CREATE DATABASE springbootdb;

DROP TABLE IF EXISTS  `city`;
CREATE TABLE `city` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT COMMENT '城市编号',
  `province_id` int(10) unsigned  NOT NULL COMMENT '省份编号',
  `city_name` varchar(25) DEFAULT NULL COMMENT '城市名称',
  `description` varchar(25) DEFAULT NULL COMMENT '描述',
  PRIMARY KEY (`id`),
  UNIQUE INDEX `UNI_IDX_ID` (`province_id`,`city_name`) USING BTREE ,
  INDEX `IDX_USER_ID` (`province_id`) USING BTREE
)
  ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;


