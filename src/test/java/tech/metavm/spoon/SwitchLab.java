package tech.metavm.spoon;

import java.io.Serializable;

public class SwitchLab {

    public Object test(Object obj) {
        return foo(switch (obj) {
            case String s -> s;
            case Integer i -> i;
            case null -> null;
            default -> "default";
        });
    }

    public Object foo(Serializable obj) {
        return obj;
    }


}
