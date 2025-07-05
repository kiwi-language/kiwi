package org.metavm.util;

import lombok.extern.slf4j.Slf4j;
import org.metavm.common.ErrorCode;
import org.metavm.object.instance.core.IInstanceContext;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.Value;
import org.metavm.util.profile.Profiler;

import javax.annotation.Nullable;
import java.util.*;

@Slf4j
public class ContextUtil {

    private static class ContextInfo {
        private Long metaVersion;
        @Nullable String clientId;
        Id platformUserId;
        Id userId;
        long appId = -1L;
        // Add randomness to challenge unit tests
        long nextTmpId = Utils.randomInt(1000000);
        String token;
        private Profiler profiler = new Profiler();
        private final Map<String, Value> userData = new HashMap<>();
        private IInstanceContext context;
        private int contextFinishCount;
        private final List<String> finishedContexts = new ArrayList<>();
        private boolean isDDL;
        private boolean waitForEsSync;
        private boolean debugging;

        long nextTmpId() {
            return nextTmpId++;
        }

        void enterApp(long appId, Id appUserId) {
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
            this.platformUserId = null;
        }

        int getContextFinishCount() {
            return contextFinishCount;
        }

        void incrementContextFinishCount() {
            contextFinishCount++;
        }

        public boolean isDDL() {
            return isDDL;
        }

        public void setDDL(boolean DDL) {
            isDDL = DDL;
        }

        void setUserData(String key, Value value) {
            userData.put(key, value);
        }

        Value getUserData(String key) {
            return userData.getOrDefault(key, Instances.nullInstance());
        }

        public IInstanceContext getContext() {
            return context;
        }

        public void resetProfiler() {
            profiler = new Profiler();
        }

        public List<String> getFinishedContexts() {
            return Collections.unmodifiableList(finishedContexts);
        }

        public void addFinishedContext(String name) {
            finishedContexts.add(name);
        }

    }

    private static final ThreadLocal<ContextInfo> THREAD_LOCAL = new ThreadLocal<>();

    public static long getAppId() {
        return getContextInfo().appId;
    }

    public static Id getUserId() {
        return getContextInfo().userId;
    }

    public static Id getPlatformUserId() {
        return getContextInfo().platformUserId;
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
        return getContextInfo().userId != null;
    }

    private static ContextInfo getContextInfo() {
        var contextInfo = THREAD_LOCAL.get();
        if(contextInfo == null) {
            contextInfo = new ContextInfo();
            THREAD_LOCAL.set(contextInfo);
        }
        return contextInfo;
    }

    public static void setWaitForSearchSync(boolean waitForEsSync) {
        getContextInfo().waitForEsSync = waitForEsSync;
    }

    public static void setDebugging(boolean debugging) {
        getContextInfo().debugging = debugging;
    }

    public static boolean isDebugging() {
        return getContextInfo().debugging;
    }

    public static boolean isWaitForEsSync() {
        return getContextInfo().waitForEsSync;
    }

    public static void enterApp(long appId, Id appUserId) {
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

    public static void setUserId(Id userId) {
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
        clientInfo.userId = null;
        clientInfo.clientId = null;
        clientInfo.appId = -1L;
    }

    public static void resetProfiler() {
        getContextInfo().resetProfiler();
    }

    public static void setContext(IInstanceContext context) {
        getContextInfo().context = context;
    }

    public static void setUserData(String key, Value value) {
        getContextInfo().setUserData(key, value);
    }

    public static Value getUserData(String key) {
        return getContextInfo().getUserData(key);
    }

    public static IInstanceContext getEntityContext() {
        return getContextInfo().getContext();
    }

    public static String getToken() {
        return getContextInfo().token;
    }

    public static void setToken(String token) {
        getContextInfo().token = token;
    }

    public static int getContextFinishCount() {
        return getContextInfo().getContextFinishCount();
    }

    public static void incrementContextFinishCount() {
        getContextInfo().incrementContextFinishCount();
    }

    public static void addFinishedContext(String name) {
        getContextInfo().addFinishedContext(name);
    }

    public static List<String> getFinishedContexts() {
        return getContextInfo().getFinishedContexts();
    }

    public static void setDDL(boolean isDDL) {
        getContextInfo().setDDL(isDDL);
    }

    public static boolean isDDL() {
        return getContextInfo().isDDL();
    }

}
