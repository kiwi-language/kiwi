package org.manul.util;

import org.manul.object.instance.core.Id;

public class TestConstants {

    public static final String TARGET = TestUtils.getResourcePath("target");
    public static String APP_NAME;
    public static volatile long APP_ID = 100L;

    public static Id USER_ID;

    public static Id getAppId() {
        return Id.getAppId(APP_ID);
    }

}
