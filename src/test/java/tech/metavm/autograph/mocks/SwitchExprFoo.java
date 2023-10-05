package tech.metavm.autograph.mocks;

public class SwitchExprFoo {

    public String test(Object value) {
        return switch (value) {
            case String str -> str;
            case Long l -> {
                if (l != 0) {
                    yield l + "L";
                } else {
                    yield "zero";
                }
            }
            default -> value.toString();
        };
    }

}
