package tech.metavm.util;

import tech.metavm.common.ErrorCode;
import tech.metavm.util.profile.Profiler;

import javax.annotation.Nullable;

public class ContextUtil {

    private static class ContextInfo {
        private Long metaVersion;
        @Nullable String clientId;
        long platformUserId = -1L;
        long userId = -1L;
        long appId = -1L;
        long nextTmpId = 1L;
        private Profiler profiler = new Profiler();

        long nextTmpId() {
            return nextTmpId++;
        }

        void enterApp(long appId, long appUserId) {
            if(this.appId == -1L)
                throw new BusinessException(ErrorCode.INVALID_TOKEN);
            if(this.appId != Constants.PLATFORM_APP_ID)
                throw new BusinessException(ErrorCode.REENTERING_APP);
            platformUserId = this.userId;
            this.appId = appId;
            this.userId = appUserId;
        }

        void exitApp() {
            if(this.appId == -1L || this.appId == Constants.PLATFORM_APP_ID)
                throw new BusinessException(ErrorCode.NOT_IN_APP);
            this.appId = Constants.PLATFORM_APP_ID;
            this.userId = platformUserId;
            this.platformUserId = -1L;
        }

        public void resetProfiler() {
            profiler = new Profiler();
        }

    }

    private static final ThreadLocal<ContextInfo> THREAD_LOCAL = new ThreadLocal<>();

    public static long getAppId() {
        return getContextInfo().appId;
    }

    public static long getUserId() {
        return getContextInfo().userId;
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
        return getContextInfo().userId != -1L;
    }

    private static ContextInfo getContextInfo() {
        var contextInfo = THREAD_LOCAL.get();
        if(contextInfo == null) {
            contextInfo = new ContextInfo();
            THREAD_LOCAL.set(contextInfo);
        }
        return contextInfo;
    }

    public static void enterApp(long appId, long appUserId) {
        getContextInfo().enterApp(appId, appUserId);
    }

    public static void exitApp() {
        getContextInfo().exitApp();
    }

    public static Profiler getProfiler() {
        return getContextInfo().profiler;
    }

    public static long nextTmpId() {
        return getContextInfo().nextTmpId();
    }

    public static void initContextInfo() {
        THREAD_LOCAL.set(new ContextInfo());
    }

    public static void setUserId(long userId) {
        getContextInfo().userId = userId;
    }

    public static void setAppId(long appId) {
        getContextInfo().appId = appId;
    }

    public static void clearContextInfo() {
        THREAD_LOCAL.remove();
    }

    public static void resetLoginInfo() {
        var clientInfo = getContextInfo();
        clientInfo.userId = -1L;
        clientInfo.clientId = null;
        clientInfo.appId = -1L;
    }

    public static void resetProfiler() {
        getContextInfo().resetProfiler();
    }

}
