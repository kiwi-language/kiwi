package org.metavm.util;

public class Constants {

    public static final long ROOT_APP_ID = 1L;

    public static final long PLATFORM_APP_ID = 2L;

    public static final String ROOT_APP_NAME = "root";

    public static final String PLATFORM_APP_NAME = "platform";

    public final static String RESOURCE_CP_ROOT = "/Users/leen/workspace/kiwi/model/src/main/resources";

    public final static String RESOURCE_TARGET_CP_ROOT = "/Users/leen/workspace/kiwi/model/target/classes";

    public static final String DEFAULT_ADMIN_NAME = "admin";

    public static final String ADMIN_ROLE_NAME = "admin";

    public static final int MAX_INHERITANCE_DEPTH = 8;

    public static final String ROOT_ADMIN_PASSWORD = "123456";
    public static final String ROOT_ADMIN_LOGIN_NAME = "root";
    public static final String PLATFORM_ADMIN_LOGIN_NAME = "platform";
    public static final String PLATFORM_ADMIN_PASSWORD = "123456";
    public static final String ID_PREFIX = "$$";
    public static final String CONSTANT_TMP_ID_PREFIX = "$_$";

    public static final int BATCH_SIZE = 3000;
//    public static final String DEFAULT_HOST = "https://api.metavm.tech/rest";
    public static final String DEFAULT_HOST = "http://localhost:8080";

    public final static long DEFAULT_SESSION_TIMEOUT = 5000L;
    public static long SESSION_TIMEOUT = DEFAULT_SESSION_TIMEOUT;
    public static long DDL_SESSION_TIMEOUT = 64000;

    public static final String RUN_METHOD_NAME = "__run__";

    public static volatile boolean maintenanceDisabled;

    private Constants() {}

    public static String removeIdPrefix(String str) {
        assert str.startsWith(ID_PREFIX);
        return str.substring(ID_PREFIX.length());
    }

    public static String addIdPrefix(String id) {
        return ID_PREFIX + id;
    }

    public static boolean isIdPrefixed(String str) {
        return str.startsWith(ID_PREFIX);
    }

    public static void disableMaintenance() {
        maintenanceDisabled = true;
    }

    public static void enableMaintenance() {
        maintenanceDisabled = false;
    }

}
