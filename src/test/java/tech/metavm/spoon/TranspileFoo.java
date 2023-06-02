package tech.metavm.spoon;

import java.util.Map;

public class TranspileFoo {

    public void testLambda(Map<String, Object> map) {
        map.forEach((k, v) -> System.out.println(k + ": " + v));
    }

}
