package switch_;

public class TypePatternSwitchFoo {

    public static String test() {
        var foo = new Foo("foo");
        return print(foo);
    }

    public static String print(Object o) {
        String name;
        switch(o) {
            case Foo foo -> name = foo.name;
            case Bar bar -> name = bar.code;
            default -> name = "Invalid object";
        }
        return name;
    }

    public record Foo(String name) {}

    public record Bar(String code) {}

}
