package switch_;

import org.metavm.api.lang.Lang;

public class TypePatternSwitchFoo {

    public static void test() {
        var foo = new Foo("foo");
        print(foo);
    }

    public static void print(Object o) {
        switch(o) {
            case Foo foo -> Lang.print(foo.name);
            case Bar bar -> Lang.print(bar.code);
            default -> Lang.print("Invalid object");
        }
    }

    public record Foo(String name) {}

    public record Bar(String code) {}

}
