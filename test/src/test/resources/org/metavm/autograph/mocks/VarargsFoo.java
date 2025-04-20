package org.metavm.autograph.mocks;

public class VarargsFoo {

    public static void foo(String s, String... args) {

    }

    public static void test() {
        VarargsFoo.foo("a");
        VarargsFoo.foo("a", "b", "c");
        VarargsFoo.foo("a", new String[] {"b", "c"});
    }

}
