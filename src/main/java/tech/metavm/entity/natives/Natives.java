package tech.metavm.entity.natives;

import tech.metavm.util.NncUtils;

import java.util.Map;

public class Natives {

    private static final Map<String, Class<?>> nativeClassMap = Map.of(
            "Map", MapNative.class,
            "List", ListNative.class,
            "Set", SetNative.class,
            "IteratorImpl", IteratorImplNative.class
    );

    public static Class<?> getNative(String nativeClas) {
        return NncUtils.requireNonNull(nativeClassMap.get(nativeClas),
                "Can't find native class for templateName '" + nativeClas + "'");
    }

}
