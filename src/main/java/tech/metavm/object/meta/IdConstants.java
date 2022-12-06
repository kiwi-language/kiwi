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

    // Block constants

    public static final long TYPE_BASE = CLASS_REGION_BASE + 1000L;

    public static final long NULLABLE_TYPE_BASE = TYPE_BASE + DEFAULT_BLOCK_SIZE;

    public static final long ARRAY_TYPE_BASE = NULLABLE_TYPE_BASE + DEFAULT_BLOCK_SIZE;

    public static final long FIELD_BASE = ARRAY_TYPE_BASE + DEFAULT_BLOCK_SIZE;

    // Standard type ids and field ids

    public static final long OBJECT = TYPE_BASE + 1L;

    public static final class ENUM {

        public static final long ID = TYPE_BASE + 2L;

        public static final long NAME_ID = FIELD_BASE + 100L;

        public static final long ORDINAL_ID = FIELD_BASE + 101L;

    }

    public static final long ENTITY = TYPE_BASE + 3L;

    public static final long INT = TYPE_BASE + 4L;

    public static final long LONG = TYPE_BASE + 5L;

    public static final long DOUBLE = TYPE_BASE + 7L;

    public static final long BOOL = TYPE_BASE + 8L;

    public static final long STRING = TYPE_BASE + 9L;

    public static final long TIME = TYPE_BASE + 10L;

    public static final long DATE = TYPE_BASE + 11L;

    public static final long PASSWORD = TYPE_BASE + 12L;

    public static final long ARRAY = TYPE_BASE + 18L;

    public static final long NULL = TYPE_BASE + 19L;

    public static final long RECORD = TYPE_BASE + 100L;

    private IdConstants() {}

}
