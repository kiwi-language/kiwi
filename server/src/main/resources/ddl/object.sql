-- MySQL dump 10.13  Distrib 8.0.30, for macos12 (arm64)
--
-- Host: localhost    Database: object
-- ------------------------------------------------------
-- Server version	8.0.30

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `choice_option`
--

DROP TABLE IF EXISTS `choice_option`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `choice_option` (
  `app_id` bigint NOT NULL COMMENT '租户ID',
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `deleted_at` bigint NOT NULL DEFAULT '0' COMMENT '删除时间戳',
  `name` varchar(64) NOT NULL COMMENT '名称',
  `order` int NOT NULL COMMENT '顺序',
  `field_id` bigint NOT NULL COMMENT '属性ID',
  `default_selected` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='枚举项';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `constraint`
--

DROP TABLE IF EXISTS `constraint`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `constraint` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `type_id` bigint NOT NULL,
  `kind` int NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted_at` bigint NOT NULL DEFAULT '0',
  `param` varchar(1024) DEFAULT NULL,
  `message` varchar(64) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1000500012 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `field`
--

DROP TABLE IF EXISTS `field`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `field` (
  `app_id` bigint NOT NULL COMMENT '租户ID',
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `name` varchar(64) NOT NULL COMMENT '名称',
  `declaring_type_id` bigint NOT NULL COMMENT '类ID',
  `deleted_at` bigint NOT NULL DEFAULT '0' COMMENT '删除时间戳',
  `access` int NOT NULL,
  `type` int DEFAULT NULL,
  `target_id` bigint DEFAULT NULL COMMENT '关联模型ID',
  `unique` tinyint(1) NOT NULL DEFAULT '0',
  `required` tinyint(1) DEFAULT NULL,
  `default_value` varchar(256) DEFAULT NULL,
  `column_name` varchar(8) DEFAULT NULL COMMENT '列名',
  `as_title` tinyint(1) NOT NULL DEFAULT '0',
  `multi_valued` tinyint(1) DEFAULT NULL,
  `type_id` bigint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `type_id_fk` (`type_id`),
  CONSTRAINT `type_id_fk` FOREIGN KEY (`type_id`) REFERENCES `type` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=1000400013 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `flow`
--

DROP TABLE IF EXISTS `flow`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `flow` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `app_id` bigint NOT NULL,
  `name` varchar(64) NOT NULL,
  `type_id` bigint NOT NULL,
  `deleted_at` bigint NOT NULL DEFAULT '0',
  `root_scope_id` bigint NOT NULL DEFAULT '0',
  `input_type_id` bigint NOT NULL DEFAULT '-1',
  `output_type_id` bigint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `flow_input_type_fk` (`input_type_id`),
  KEY `flow_output_type_fk` (`output_type_id`),
  CONSTRAINT `flow_input_type_fk` FOREIGN KEY (`input_type_id`) REFERENCES `type` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `flow_output_type_fk` FOREIGN KEY (`output_type_id`) REFERENCES `type` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=2606 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `id_block`
--

DROP TABLE IF EXISTS `id_block`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `id_block` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `app_id` bigint NOT NULL,
  `type_id` bigint NOT NULL,
  `start` bigint NOT NULL,
  `end` bigint NOT NULL,
  `next` bigint NOT NULL,
  `active` tinyint(1) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_end` (`end`)
) ENGINE=InnoDB AUTO_INCREMENT=8301034834169598227 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `id_region`
--

DROP TABLE IF EXISTS `id_region`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `id_region` (
  `type_category` int NOT NULL,
  `start` bigint NOT NULL,
  `end` bigint NOT NULL,
  `next` bigint NOT NULL,
  PRIMARY KEY (`type_category`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `id_set`
--

DROP TABLE IF EXISTS `id_set`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `id_set` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `size` int NOT NULL DEFAULT '0',
  `next_seq` int NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `id_set_node`
--

DROP TABLE IF EXISTS `id_set_node`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `id_set_node` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `set_id` bigint NOT NULL,
  `value` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `set_id` (`set_id`,`value`),
  KEY `order_key` (`set_id`,`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `index_entry`
--

DROP TABLE IF EXISTS `index_entry`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `index_entry` (
  `app_id` bigint NOT NULL,
  `constraint_id` bigint NOT NULL,
  `column0` varchar(64) DEFAULT NULL,
  `column1` varchar(64) DEFAULT NULL,
  `column2` varchar(64) DEFAULT NULL,
  `column3` varchar(64) DEFAULT NULL,
  `column4` varchar(64) DEFAULT NULL,
  `column_x_present` tinyint(1) DEFAULT '0',
  `column_x` bigint NOT NULL DEFAULT '0',
  `instance_id` bigint NOT NULL,
  KEY `idx` (`app_id`,`constraint_id`,`column0`,`column1`,`column2`,`column3`,`column4`,`column_x_present`,`column_x`,`instance_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `instance`
--

DROP TABLE IF EXISTS `instance`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `instance` (
  `id` bigint NOT NULL,
  `app_id` bigint NOT NULL,
  `type_id` bigint NOT NULL,
  `title` varchar(64) DEFAULT NULL,
  `data` json DEFAULT NULL,
  `version` bigint NOT NULL DEFAULT '0',
  `sync_version` bigint NOT NULL DEFAULT '0',
  `deleted_at` bigint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `instance_array`
--

DROP TABLE IF EXISTS `instance_array`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `instance_array` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `app_id` bigint NOT NULL COMMENT '租户ID',
  `type_id` bigint NOT NULL COMMENT '类型ID',
  `length` int NOT NULL DEFAULT '0',
  `element_as_child` tinyint(1) NOT NULL DEFAULT '0',
  `elements` json NOT NULL,
  `version` bigint NOT NULL DEFAULT '0',
  `sync_version` bigint NOT NULL DEFAULT '0',
  `deleted_at` bigint NOT NULL DEFAULT '0' COMMENT '删除时间戳',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=7205760404692812 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `instance_bak`
--

DROP TABLE IF EXISTS `instance_bak`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `instance_bak` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `app_id` bigint NOT NULL COMMENT '租户ID',
  `type_id` bigint NOT NULL COMMENT '类ID',
  `deleted_at` bigint NOT NULL DEFAULT '0' COMMENT '删除时间戳',
  `l0` bigint DEFAULT NULL,
  `l1` bigint DEFAULT NULL,
  `l2` bigint DEFAULT NULL,
  `l3` bigint DEFAULT NULL,
  `l4` bigint DEFAULT NULL,
  `l5` bigint DEFAULT NULL,
  `l6` bigint DEFAULT NULL,
  `l7` bigint DEFAULT NULL,
  `l8` bigint DEFAULT NULL,
  `l9` bigint DEFAULT NULL,
  `i0` int DEFAULT NULL,
  `i1` int DEFAULT NULL,
  `i2` int DEFAULT NULL,
  `i3` int DEFAULT NULL,
  `i4` int DEFAULT NULL,
  `i5` int DEFAULT NULL,
  `i6` int DEFAULT NULL,
  `i7` int DEFAULT NULL,
  `i8` int DEFAULT NULL,
  `i9` int DEFAULT NULL,
  `s0` varchar(64) DEFAULT NULL,
  `s1` varchar(64) DEFAULT NULL,
  `s2` varchar(64) DEFAULT NULL,
  `s3` varchar(64) DEFAULT NULL,
  `s4` varchar(64) DEFAULT NULL,
  `s5` varchar(64) DEFAULT NULL,
  `s6` varchar(64) DEFAULT NULL,
  `s7` varchar(64) DEFAULT NULL,
  `s8` varchar(64) DEFAULT NULL,
  `s9` varchar(64) DEFAULT NULL,
  `b0` tinyint(1) DEFAULT NULL,
  `b1` tinyint(1) DEFAULT NULL,
  `b2` tinyint(1) DEFAULT NULL,
  `b3` tinyint(1) DEFAULT NULL,
  `b4` tinyint(1) DEFAULT NULL,
  `b5` tinyint(1) DEFAULT NULL,
  `b6` tinyint(1) DEFAULT NULL,
  `b7` tinyint(1) DEFAULT NULL,
  `b8` tinyint(1) DEFAULT NULL,
  `b9` tinyint(1) DEFAULT NULL,
  `d0` double DEFAULT NULL,
  `d1` double DEFAULT NULL,
  `d2` double DEFAULT NULL,
  `d3` double DEFAULT NULL,
  `d4` double DEFAULT NULL,
  `d5` double DEFAULT NULL,
  `d6` double DEFAULT NULL,
  `d7` double DEFAULT NULL,
  `d8` double DEFAULT NULL,
  `d9` double DEFAULT NULL,
  `title` varchar(64) NOT NULL DEFAULT '',
  `version` bigint unsigned NOT NULL DEFAULT '0',
  `sync_version` bigint unsigned NOT NULL DEFAULT '0',
  `t1` varchar(1024) DEFAULT NULL,
  `key` varchar(64) DEFAULT NULL,
  `t0` varchar(1024) DEFAULT NULL,
  `o0` json DEFAULT NULL,
  `o1` json DEFAULT NULL,
  `o2` json DEFAULT NULL,
  `o3` json DEFAULT NULL,
  `o4` json DEFAULT NULL,
  `o5` json DEFAULT NULL,
  `o6` json DEFAULT NULL,
  `o7` json DEFAULT NULL,
  `o8` json DEFAULT NULL,
  `o9` json DEFAULT NULL,
  `l10` bigint DEFAULT NULL,
  `l11` bigint DEFAULT NULL,
  `l12` bigint DEFAULT NULL,
  `l13` bigint DEFAULT NULL,
  `l14` bigint DEFAULT NULL,
  `l15` bigint DEFAULT NULL,
  `l16` bigint DEFAULT NULL,
  `l17` bigint DEFAULT NULL,
  `l18` bigint DEFAULT NULL,
  `l19` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `instance_type_id_fk` (`type_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1001200030 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `node`
--

DROP TABLE IF EXISTS `node`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `node` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `app_id` bigint NOT NULL,
  `name` varchar(64) NOT NULL,
  `flow_id` bigint NOT NULL,
  `type` int NOT NULL,
  `prev_id` bigint DEFAULT NULL,
  `output_type_id` bigint DEFAULT NULL,
  `deleted_at` bigint NOT NULL DEFAULT '0',
  `param` varchar(2048) DEFAULT NULL,
  `scope_id` bigint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2610 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `nullable_instance`
--

DROP TABLE IF EXISTS `nullable_instance`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `nullable_instance` (
  `id` bigint NOT NULL,
  `app_id` bigint NOT NULL,
  `type_id` bigint NOT NULL,
  `value_id` bigint DEFAULT NULL,
  `version` bigint unsigned NOT NULL DEFAULT '0',
  `sync_version` bigint unsigned NOT NULL DEFAULT '0',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted_at` bigint unsigned NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `reference`
--

DROP TABLE IF EXISTS `reference`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `reference` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `app_id` bigint NOT NULL,
  `field_id` bigint NOT NULL,
  `source_id` bigint NOT NULL,
  `target_id` bigint NOT NULL,
  `kind` int NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_idx` (`app_id`,`field_id`,`target_id`,`source_id`),
  KEY `target_idx` (`app_id`,`kind`,`target_id`)
) ENGINE=InnoDB AUTO_INCREMENT=11553 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `relation`
--

DROP TABLE IF EXISTS `relation`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `relation` (
  `app_id` bigint NOT NULL COMMENT '租户ID',
  `src_instance_id` bigint NOT NULL COMMENT '起点实例ID',
  `field_id` bigint NOT NULL COMMENT '属性ID',
  `dest_instance_id` bigint NOT NULL COMMENT '终点实例ID',
  PRIMARY KEY (`app_id`,`src_instance_id`,`field_id`,`dest_instance_id`),
  KEY `dest` (`app_id`,`dest_instance_id`,`field_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='实例关系';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `scope`
--

DROP TABLE IF EXISTS `scope`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `scope` (
  `id` bigint NOT NULL,
  `deleted_at` bigint NOT NULL DEFAULT '0',
  `flow_id` bigint NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `application`
--

DROP TABLE IF EXISTS `application`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `application` (
  `id` bigint NOT NULL,
  `name` varchar(64) NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted_at` bigint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `type`
--

DROP TABLE IF EXISTS `type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `type` (
  `app_id` bigint NOT NULL COMMENT '租户ID',
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id',
  `name` varchar(64) NOT NULL COMMENT '名称',
  `deleted_at` bigint NOT NULL DEFAULT '0' COMMENT '删除时间戳',
  `desc` varchar(128) DEFAULT NULL COMMENT '描述',
  `ephemeral` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否临时',
  `category` tinyint unsigned DEFAULT NULL COMMENT '类型 0: 默认 1: 枚举 2: 接口',
  `anonymous` tinyint(1) NOT NULL DEFAULT '0',
  `type_argument_ids` varchar(128) DEFAULT NULL,
  `raw_type_id` bigint DEFAULT NULL,
  `super_type_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `element_type_id_idx` (`app_id`),
  KEY `raw_type_id_fk` (`raw_type_id`),
  CONSTRAINT `raw_type_id_fk` FOREIGN KEY (`raw_type_id`) REFERENCES `type` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=1000300009 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2023-01-16  2:57:20
