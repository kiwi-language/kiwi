package org.metavm.util;

import org.metavm.object.instance.core.Id;

public class TestConstants {

    public static final String TARGET = TestUtils.getResourcePath("target");
    public static volatile long APP_ID = 100L;

    public static final long USER_ID = 1004L;

    public static Id getAppId() {
        return Id.getAppId(APP_ID);
    }

}
