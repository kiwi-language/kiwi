package tech.metavm.util;

import tech.metavm.common.ErrorCode;
import tech.metavm.util.profile.Profiler;

import javax.annotation.Nullable;

public class ContextUtil {

    private static class ContextInfo {
        private Long metaVersion;
        @Nullable String clientId;
        LoginInfo loginInfo;
        private final Profiler profiler = new Profiler();
    }

    private record LoginInfo(long tenantId, long userId) {
    }

    private static final ThreadLocal<ContextInfo> THREAD_LOCAL = new ThreadLocal<>();

    public static long getTenantId() {
        return getLoginInfo().tenantId;
    }

    public static long getUserId() {
        return getLoginInfo().userId;
    }

    public static @Nullable String getClientId() {
        return getContextInfo().clientId;
    }

    public static Long getMetaVersion() {
        return getContextInfo().metaVersion;
    }

    public static void setMetaVersion(long metaVersion) {
        getContextInfo().metaVersion = metaVersion;
    }

    public static void setClientId(String clientId) {
        getContextInfo().clientId = clientId;
    }

    public static boolean isLoggedIn() {
        return getContextInfo().loginInfo != null;
    }

    private static ContextInfo getContextInfo() {
        var contextInfo = THREAD_LOCAL.get();
        if(contextInfo == null) {
            contextInfo = new ContextInfo();
            THREAD_LOCAL.set(contextInfo);
        }
        return contextInfo;
    }

    public static Profiler getProfiler() {
        return getContextInfo().profiler;
    }

    private static LoginInfo getLoginInfo() {
        return NncUtils.assertNonNull(getContextInfo().loginInfo, ErrorCode.VERIFICATION_FAILED);
    }

    public static boolean isContextAvailable() {
        return THREAD_LOCAL.get() != null;
    }

    public static void initContextInfo() {
        THREAD_LOCAL.set(new ContextInfo());
    }

    public static void setLoginInfo(long tenantId, long userId) {
        getContextInfo().loginInfo = new LoginInfo(tenantId, userId);
    }

    public static void clearContextInfo() {
        THREAD_LOCAL.remove();
    }

}
