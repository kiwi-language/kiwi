package tech.metavm.util;

public class SQLStatements {

    public static final String CREATE_INSTANCE = """
            CREATE TABLE `instance` (
              `id` bigint NOT NULL,
              `app_id` bigint NOT NULL,
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
                  `app_id` bigint NOT NULL COMMENT '租户ID',
                  `type_id` bigint NOT NULL COMMENT '类型ID',
                  `length` int NOT NULL DEFAULT '0',
                  `element_as_child` bool NOT NULL default false,
                  `elements` text NOT NULL,
                  version bigint not null default 0,
                  sync_version bigint not null default 0,
                  `deleted_at` bigint NOT NULL DEFAULT '0' COMMENT '删除时间戳',
                  PRIMARY KEY (`id`)
            )""";

    public static final String CREATE_INDEX_ENTRY = """
            CREATE TABLE `index_entry` (
                  `app_id` bigint NOT NULL,
                  `index_id` bigint NOT NULL,
                  `column0` binary DEFAULT NULL,
                  `column1` binary DEFAULT NULL,
                  `column2` binary DEFAULT NULL,
                  `column3` binary DEFAULT NULL,
                  `column4` binary DEFAULT NULL,
                  `column5` binary DEFAULT NULL,
                  `column6` binary DEFAULT NULL,
                  `column7` binary DEFAULT NULL,
                  `column8` binary DEFAULT NULL,
                  `column9` binary DEFAULT NULL,
                  `column10` binary DEFAULT NULL,
                  `column11` binary DEFAULT NULL,
                  `column12` binary DEFAULT NULL,
                  `column13` binary DEFAULT NULL,
                  `column14` binary DEFAULT NULL,
                  `instance_id` bigint NOT NULL
            );""";

    public static final String CREATE_REFERENCE = """
            CREATE TABLE `reference` (
                                         `id` bigint NOT NULL AUTO_INCREMENT,
                                         `app_id` bigint NOT NULL,
                                         `field_id` bigint NOT NULL,
                                         `source_id` bigint NOT NULL,
                                         `target_id` bigint NOT NULL,
                                         `kind` int NOT NULL,
                                         PRIMARY KEY (`id`),
                                         UNIQUE KEY `unique_idx` (`app_id`,`field_id`,`target_id`,`source_id`)
            )
            """;

}
