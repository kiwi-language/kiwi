package org.metavm.autograph.mocks;


import org.metavm.api.Entity;

@Entity(compiled = true)
public class AstSwitchFoo {

    private String title;

    public String test(Object value) {
        return switch (value) {
            case String str -> {
                yield str;
            }
            case Long l -> {
                if(l == 0L) {
                    yield "zero";
                }
                else {
                    yield l + "L";
                }
            }
            case Integer i -> {
                yield i + "";
            }
            case AstSwitchFoo s -> {
                yield s.title;
            }
            default -> {
                yield "default";
            }
        };
    }

}
