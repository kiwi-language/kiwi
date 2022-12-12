package tech.metavm.object.meta;

public class IdConstants {

    public static final long DEFAULT_BLOCK_SIZE = 100000L;

    public static final long SYSTEM_RESERVE_PER_REGION = 1000000000L;

    // Region constants

    public static final long CLASS_REGION_BASE = 0L;

    public static final long CLASS_REGION_END = Long.MAX_VALUE / 10 * 5;

    public static final long ARRAY_REGION_BASE = Long.MAX_VALUE / 10 * 8;

    public static final long ARRAY_REGION_END = Long.MAX_VALUE / 10 * 9 - 1;

    public static final long ENUM_REGION_BASE = Long.MAX_VALUE / 10 * 9;

    public static final long ENUM_REGION_END = Long.MAX_VALUE;

    private IdConstants() {}

}
