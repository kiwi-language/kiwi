package tech.metavm.util;

public class TestContext {

    private static long appId = TestConstants.APP_ID;

    public static long getAppId() {
        return appId;
    }

    public static void setAppId(long appId) {
        TestContext.appId = appId;
    }

    public static void resetAppId() {
        appId = TestConstants.APP_ID;
    }

}
