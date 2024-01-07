package tech.metavm.object.type;

public class IdConstants {

    public static final long MAX_ID = 9007199254740991L;

    public static final long DEFAULT_BLOCK_SIZE = 100000L;

    public static final long SYSTEM_RESERVE_PER_REGION = 1000000000L;

    public static final long ROOT_APP_ID = 1L;

    public static final long PLATFORM_APP_ID = 2L;

    // Region constants

    public static final long CLASS_REGION_BASE = 0L;

    public static final long CLASS_REGION_END = MAX_ID / 10 * 5;

    public static final long READ_WRITE_ARRAY_REGION_BASE = MAX_ID / 100 * 80;

    public static final long READ_WRITE_ARRAY_REGION_END = MAX_ID / 100 * 85 - 1;

    public static final long READ_ONLY_ARRAY_REGION_BASE = MAX_ID / 100 * 85;

    public static final long READ_ONLY_ARRAY_REGION_END = MAX_ID / 100 * 87 - 1;

    public static final long CHILD_ARRAY_REGION_BASE = MAX_ID / 100 * 87;

    public static final long CHILD_ARRAY_REGION_END = MAX_ID / 100 * 90 - 1;

    public static final long ENUM_REGION_BASE = MAX_ID / 10 * 9;

    public static final long ENUM_REGION_END = MAX_ID;

    private IdConstants() {}

    public static boolean isBuiltinAppId(long appId) {
        return appId == ROOT_APP_ID || appId == PLATFORM_APP_ID;
    }

}
