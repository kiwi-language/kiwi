package tech.metavm.spoon;

import java.util.Map;

public class MapOfLab {

    public Map<String, Object> test() {
        return Map.of(
                "k1", "v1",
                "k2", 100L
        );
    }
}
