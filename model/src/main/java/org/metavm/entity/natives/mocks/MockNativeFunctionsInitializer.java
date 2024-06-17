package org.metavm.entity.natives.mocks;

import org.metavm.entity.natives.NativeFunctions;

public class MockNativeFunctionsInitializer {

    public static void init() {
        NativeFunctions.defineSystemFunctions();
    }

}
