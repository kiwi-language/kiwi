package org.metavm.entity.natives.mocks;

import org.metavm.entity.natives.StdFunction;

public class MockNativeFunctionsInitializer {

    public static void init() {
        StdFunction.defineSystemFunctions();
    }

}
