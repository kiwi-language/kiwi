package tech.metavm.util;

public class ContextUtil {

    public static class ContextInfo {
        private final long tenantId;
        private final long userId;

        public ContextInfo(long tenantId, long userId) {
            this.tenantId = tenantId;
            this.userId = userId;
        }
    }

    private static final ThreadLocal<ContextInfo> THREAD_LOCAL = new ThreadLocal<>();

    public static long getTenantId() {
        return getContextInfo().tenantId;
    }

    public static long getUserId() {
        return getContextInfo().userId;
    }

    private static ContextInfo getContextInfo() {
        return NncUtils.requireNonNull(THREAD_LOCAL.get(), "用户未登录");
    }

    public static void setContextInfo(long tenantId, long userId) {
        THREAD_LOCAL.set(new ContextInfo(tenantId, userId));
    }

    public static void clearContextInfo() {
        THREAD_LOCAL.remove();
    }

}
