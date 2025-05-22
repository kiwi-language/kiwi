package org.metavm.util;

import javax.annotation.Nullable;
import java.util.Map;

public record ApiNamedObject(@Nullable String type, String name) {

    public static ApiNamedObject of(String type, String name) {
        return new ApiNamedObject(type, name);
    }

    public static ApiNamedObject of(String name) {
        return new ApiNamedObject(null, name);
    }

    public Map<String, Object> toMap() {
        return type != null ? Map.of("type", type, "name", name) : Map.of("name", name);
    }

}
