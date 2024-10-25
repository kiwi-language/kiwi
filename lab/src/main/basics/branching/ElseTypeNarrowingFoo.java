package branching;

public class ElseTypeNarrowingFoo {

    public static int test(Foo foo) {
        Foo f;
        if((f = foo) == null)
            return 0;
        else
            return f.value;
    }

    public record Foo(int value) {

    }

}
