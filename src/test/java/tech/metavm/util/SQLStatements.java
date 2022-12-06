package tech.metavm.util;

public class SQLStatements {

    public static final String CREATE_INSTANCE = """
                CREATE TABLE `instance` (
                  `id` bigint NOT NULL,
                  `tenant_id` bigint NOT NULL,
                  `type_id` bigint NOT NULL,
                  `title` varchar(64) DEFAULT NULL,
                  `data` text DEFAULT NULL,
                  `version` bigint NOT NULL DEFAULT '0',
                  `sync_version` bigint NOT NULL DEFAULT '0',
                  `deleted_at` bigint NOT NULL DEFAULT '0',
                  PRIMARY KEY (`id`)
                )""";

    public static final String CREATE_INSTANCE_ARRAY = """
                CREATE TABLE `instance_array` (
                      `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
                      `tenant_id` bigint NOT NULL COMMENT '租户ID',
                      `type_id` bigint NOT NULL COMMENT '类型ID',
                      `length` int NOT NULL DEFAULT '0',
                      `elements` text NOT NULL,
                      version bigint not null default 0,
                      sync_version bigint not null default 0,
                      `deleted_at` bigint NOT NULL DEFAULT '0' COMMENT '删除时间戳',
                      PRIMARY KEY (`id`)
                )""";

    public static final String CREATE_INDEX_ITEM = """
            CREATE TABLE `index_item` (
              `tenant_id` bigint NOT NULL,
              `constraint_id` bigint NOT NULL,
              `column1` varchar(64) DEFAULT NULL,
              `column2` varchar(64) DEFAULT NULL,
              `column3` varchar(64) DEFAULT NULL,
              `instance_id` bigint NOT NULL,
              `column4` varchar(64) DEFAULT NULL,
              `column5` varchar(64) DEFAULT NULL,
              UNIQUE KEY `tenant_id` (`tenant_id`,`constraint_id`,`column1`,`column2`,`column3`,`instance_id`)
            )""";

}
