package tech.metavm.spoon;

import java.util.function.Function;

public class SpoonFoo {

    public static <T,R> R get(T value, Function<T,R> function) {
        return value != null ? function.apply(value) : null;
    }

    public static void test() {
        get(new SpoonFoo(), Function.identity());
    }

}