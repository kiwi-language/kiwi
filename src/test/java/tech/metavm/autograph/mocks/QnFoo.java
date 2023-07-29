package tech.metavm.autograph.mocks;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class QnFoo {

    public Map<String, Objects> getMap(String name, Object value) {
        long l = 1;
        System.out.println(l);
        return new HashMap<>();
    }

}
