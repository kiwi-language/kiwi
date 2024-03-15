package tech.metavm.util;

import tech.metavm.application.Application;
import tech.metavm.entity.ModelDefRegistry;
import tech.metavm.object.instance.core.Id;
import tech.metavm.object.instance.core.PhysicalId;

public class Constants {

    public static final long ROOT_APP_ID = 1L;

    public static final long PLATFORM_APP_ID = 2L;

    public static final String ROOT_APP_NAME = "root";

    public static final String PLATFORM_APP_NAME = "platform";

    public final static String RESOURCE_CP_ROOT = "/Users/leen/workspace/object/model/src/main/resources";

    public final static String RESOURCE_TARGET_CP_ROOT = "/Users/leen/workspace/object/model/target/classes";

    public static final String DEFAULT_ADMIN_NAME = "管理员";

    public static final String ADMIN_ROLE_NAME = "管理员";

    public static final int MAX_INHERITANCE_DEPTH = 7;

    public static final String ROOT_ADMIN_PASSWORD = "123456";
    public static final String ROOT_ADMIN_LOGIN_NAME = "root";
    public static final String PLATFORM_ADMIN_LOGIN_NAME = "platform";
    public static final String PLATFORM_ADMIN_PASSWORD = "123456";
    public static final String CONSTANT_ID_PREFIX = "$$";
    public static final String CONSTANT_TMP_ID_PREFIX = "$_$";

    public static final int BATCH_SIZE = 3000;

    private Constants() {}

    public static Id getRootAppId() {
        return PhysicalId.of(ROOT_APP_ID, ModelDefRegistry.getType(Application.class));
    }

    public static Id getPlatformAppId() {
        return PhysicalId.of(PLATFORM_APP_ID, ModelDefRegistry.getType(Application.class));
    }

    public static Id getAppId(long id) {
        return PhysicalId.of(id, ModelDefRegistry.getType(Application.class));
    }

}
