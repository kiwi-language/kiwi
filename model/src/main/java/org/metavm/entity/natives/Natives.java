package org.metavm.entity.natives;

import java.util.Map;
import java.util.Objects;

public class Natives {

    private static final Map<String, Class<?>> nativeClassMap = Map.of(
            "Map", MapNative.class,
            "List", ListNative.class,
            "Set", HashSetNative.class,
            "IteratorImpl", IteratorImplNative.class,
            "Throwable", ThrowableNative.class,
            "Exception", ExceptionNative.class,
            "RuntimeException", RuntimeExceptionNative.class
    );

    public static Class<?> getNative(String nativeClass) {
        return Objects.requireNonNull(nativeClassMap.get(nativeClass),
                "Can't find native class for templateName '" + nativeClass + "'");
    }

}
