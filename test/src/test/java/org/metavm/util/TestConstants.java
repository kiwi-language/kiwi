package org.metavm.util;

import org.metavm.object.instance.core.Id;

public class TestConstants {

    public static final long APP_ID = 100L;

    public static final long USER_ID = 1004L;

    public static final String TEST_RESOURCE_CP_ROOT = "/Users/leen/workspace/object/src/test/resources";

    public static Id getAppId() {
        return Constants.getAppId(APP_ID);
    }

}
