package tech.metavm.autograph.mocks;


import tech.metavm.entity.EntityType;

@EntityType(value = "AstSwitchFoo", compiled = true)
public class AstSwitchFoo {

    public String test(Object value) {
        return switch (value) {
            case String str -> {
                if (!str.isEmpty()) {
                    yield str;
                }
                yield "empty";
            }
            case Long l -> {
                yield l + "L";
            }
            case Integer i -> {
                yield i + "";
            }
            case Boolean b -> {
                yield b + "";
            }
            default -> {
                yield "default";
            }
        };
    }

}
