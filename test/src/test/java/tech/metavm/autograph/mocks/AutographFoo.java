package tech.metavm.autograph.mocks;

import java.util.List;

public class AutographFoo<E extends List<E>> {

    public String test(Object object) {
        return switch (object) {
            case Integer i -> Integer.toBinaryString(i);
            case String s -> s;
            default -> object.toString();
        };
    }

}
