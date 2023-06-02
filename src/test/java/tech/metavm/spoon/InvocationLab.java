package tech.metavm.spoon;

import java.util.Map;

public class InvocationLab {

    public void testComputeIfAbsent(Map<String, Object> map, String key, Object value) {
        map.computeIfAbsent(key, k -> value);
    }

}
