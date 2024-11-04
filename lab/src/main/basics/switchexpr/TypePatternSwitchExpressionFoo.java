package switchexpr;

public class TypePatternSwitchExpressionFoo {

    public static String test(String name) {
        return toString(new Foo(name));
    }

    public static String toString(Object o) {
        return switch(o) {
            case Foo foo -> foo.name;
            case Bar bar -> bar.code;
            default -> throw new IllegalStateException("Invalid object " + o);
        };
    }

    private record Foo(String name) {}

    private record Bar(String code) {}

}
