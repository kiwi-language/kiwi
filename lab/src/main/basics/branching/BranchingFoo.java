package branching;

import javax.annotation.Nullable;

public class BranchingFoo {

    public static Long getOrDefault(@Nullable Long value, Long defaultValue) {
        Long v;
        return (v = value) != null && v > 0L ? v : defaultValue;
    }

    public static Long getOrDefault2(@Nullable Long value, Long defaultValue) {
        Long result;
        if(value != null && value > 0L)
            result = value;
        else
            result = defaultValue;
        return result;
    }

    public static boolean isNameNotNull(Foo foo) {
        return foo != null && foo.name != null;
    }

    public static boolean testIsNameNotNull() {
        return isNameNotNull(new Foo("foo"));
    }

    public record Foo(String name) {}

}
