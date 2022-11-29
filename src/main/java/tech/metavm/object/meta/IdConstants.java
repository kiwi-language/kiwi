package tech.metavm.object.meta;

public class IdConstants {

    public static final long DEFAULT_BLOCK_SIZE = 100000L;

    public static final long SYSTEM_RESERVE_PER_REGION = 1000000000L;

    // Region constants

    public static final long CLASS_REGION_BASE = 0L;

    public static final long CLASS_REGION_END = Long.MAX_VALUE / 10 * 5;

    public static final long ENUM_REGION_BASE = Long.MAX_VALUE / 10 * 9;

    public static final long ENUM_REGION_END = Long.MAX_VALUE;

    // Block constants

    public static final long TYPE_BASE = CLASS_REGION_BASE + 1000L;

    public static final long NULLABLE_TYPE_BASE = TYPE_BASE + DEFAULT_BLOCK_SIZE;

    public static final long ARRAY_TYPE_BASE = NULLABLE_TYPE_BASE + DEFAULT_BLOCK_SIZE;

    public static final long FIELD_BASE = ARRAY_TYPE_BASE + DEFAULT_BLOCK_SIZE;

    public static final long UNIQUE_CONSTRAINT_BASE = FIELD_BASE + 10 * DEFAULT_BLOCK_SIZE;

    public static final long CHECK_CONSTRAINT_BASE = UNIQUE_CONSTRAINT_BASE + DEFAULT_BLOCK_SIZE;

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

//    public static final long NULLABLE = TYPE_BASE + 20L;

    public static class TYPE {
        public static final long ID = TYPE_BASE + 20L;
    }

    public static class FIELD {
        public static final long ID = TYPE_BASE + 21L;
    }

    public static class FLOW {
        public static final long ID = TYPE_BASE + 22L;
    }

    public static class ADD_OBJECT_NODE {
        public static final long ID = TYPE_BASE + 23L;
    }

    public static class BRANCH_NODE {
        public static final long ID = TYPE_BASE + 24L;
    }

    public static class DELETE_OBJECT_NODE {
        public static final long ID = TYPE_BASE + 25L;
    }

    public static class DIRECTORY_ACCESS_NODE {
        public static final long ID = TYPE_BASE + 26L;
    }

    public static class EXCEPTION_NODE {
        public static final long ID = TYPE_BASE + 27L;
    }

    public static class GET_OBJECT_NODE {
        public static final long ID = TYPE_BASE + 28L;
    }

    public static class GET_RELATION_NODE {
        public static final long ID = TYPE_BASE + 29L;
    }

    public static class GET_UNIQUE_NODE {
        public static final long ID = TYPE_BASE + 30L;
    }

    public static class INPUT_NODE {
        public static final long ID = TYPE_BASE + 31L;
    }

    public static class LOOP_NODE {
        public static final long ID = TYPE_BASE + 32L;
    }

    public static class RETURN_NODE {
        public static final long ID = TYPE_BASE + 33L;
    }

    public static class SELF_NODE {
        public static final long ID = TYPE_BASE + 34L;
    }

    public static class SUB_FLOW_NODE {
        public static final long ID = TYPE_BASE + 35L;
    }

    public static class UPDATE_OBJECT_NODE {
        public static final long ID = TYPE_BASE + 36L;
    }

    public static class CHECK_CONSTRAINT {
        public static final long ID = TYPE_BASE + 37L;
    }

    public static class UNIQUE_CONSTRAINT {
        public static final long ID = TYPE_BASE + 38L;
    }

    public static class TENANT {
        public static final long ID = TYPE_BASE + 39L;
    }

    public static class SCOPE {
        public static final long ID = TYPE_BASE + 40L;
    }

    public static class USER {

        public static final long ID = TYPE_BASE + 50L;

        public static final long FID_NAME = FIELD_BASE + 52L;

        public static final long FID_LOGIN_NAME = FIELD_BASE + 53L;

        public static final long FID_PASSWORD = FIELD_BASE + 54L;

        public static final long FID_ROLES = FIELD_BASE + 55L;

        public static final long CID_UNIQUE_LOGIN_NAME = UNIQUE_CONSTRAINT_BASE + 58L;

    }

    public static class ROLE {

        public static final long ID = TYPE_BASE + 60L;

        public static final long FID_NAME = FIELD_BASE + 61L;

    }

    public static class MetaType {

        public static final long ID = TYPE_BASE + 61L;

        public static final long FID_NAME = FIELD_BASE + 70L;

        public static final long FID_CATEGORY = FIELD_BASE + 71L;

        public static final long FID_FIELDS = FIELD_BASE + 72L;

    }

    private IdConstants() {}

}
