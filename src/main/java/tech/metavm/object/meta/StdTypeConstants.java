package tech.metavm.object.meta;

public class StdTypeConstants {

    private StdTypeConstants() {}

    public static final long OBJECT = 1L;

    public static final long INT = 4L;

    public static final long LONG = 5L;

    public static final long DOUBLE = 7L;

    public static final long BOOL = 8L;

    public static final long STRING = 9L;

    public static final long TIME = 10L;

    public static final long DATE = 11L;

    public static final long PASSWORD = 12L;

    public static final long ARRAY = 18L;

    public static final long NULLABLE = 19L;

    public static class USER {

        public static final long ID = 50L;

        public static final long FID_NAME = 52L;

        public static final long FID_LOGIN_NAME = 53L;

        public static final long FID_PASSWORD = 54L;

        public static final long FID_ROLES = 55L;

        public static final long CID_UNIQUE_LOGIN_NAME = 58L;

    }

    public static class ROLE {

        public static final long ID = 60L;

        public static final long FID_NAME = 61L;

    }

}
