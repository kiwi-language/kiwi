package org.metavm.util;

import org.metavm.application.Application;
import org.metavm.entity.ModelDefRegistry;
import org.metavm.entity.natives.EmailSender;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.PhysicalId;

public class Constants {

    public static final byte[] EMPTY_BYTES = new byte[0];

    public static final long ROOT_APP_ID = 1L;

    public static final long PLATFORM_APP_ID = 2L;

    public static final String ROOT_APP_NAME = "root";

    public static final String PLATFORM_APP_NAME = "platform";

    public final static String RESOURCE_CP_ROOT = "/Users/leen/workspace/object/model/src/main/resources";

    public final static String RESOURCE_TARGET_CP_ROOT = "/Users/leen/workspace/object/model/target/classes";

    public static final String DEFAULT_ADMIN_NAME = "admin";

    public static final String ADMIN_ROLE_NAME = "admin";

    public static final int MAX_INHERITANCE_DEPTH = 7;

    public static final String ROOT_ADMIN_PASSWORD = "123456";
    public static final String ROOT_ADMIN_LOGIN_NAME = "root";
    public static final String PLATFORM_ADMIN_LOGIN_NAME = "platform";
    public static final String PLATFORM_ADMIN_PASSWORD = "123456";
    public static final String ID_PREFIX = "$$";
    public static final String CONSTANT_TMP_ID_PREFIX = "$_$";

    public static final int BATCH_SIZE = 3000;
//    public static final String DEFAULT_HOST = "https://metavm.tech/rest";
    public static final String DEFAULT_HOST = "http://localhost:8080";
    public static EmailSender emailSender;

    public final static long DEFAULT_SESSION_TIMEOUT = 2000L;
    public static long SESSION_TIMEOUT = DEFAULT_SESSION_TIMEOUT;
    public static long DDL_SESSION_TIMEOUT = 64000;

    public static final String RUN_METHOD_NAME = "__run__";

    public static final int CLASS_MAGIC = 0x1F4D5B3A;

    private Constants() {}

    public static Id getRootAppId() {
        return PhysicalId.of(ROOT_APP_ID, 0L, ModelDefRegistry.getType(Application.class));
    }

    public static Id getPlatformAppId() {
        return PhysicalId.of(PLATFORM_APP_ID, 0L, ModelDefRegistry.getType(Application.class));
    }

    public static Id getAppId(long id) {
        return PhysicalId.of(id, 0L, ModelDefRegistry.getType(Application.class));
    }

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

}
