package tech.metavm.autograph.mocks;

public class AutographFoo {

    public String test(Object object) {
        return switch (object) {
            case Integer i -> Integer.toBinaryString(i);
            case String s -> s;
            default -> object.toString();
        };
    }

}
