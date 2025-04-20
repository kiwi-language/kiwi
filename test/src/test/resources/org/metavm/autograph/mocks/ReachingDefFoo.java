package org.metavm.autograph.mocks;

public class ReachingDefFoo {

    public void test(String name) {
        long l = 1;
        System.out.println(name + ": " + l);
        l++;
        System.out.println(l);
    }

    private String name = "hello";

    public void testField() {
        name = "World";
        System.out.println(name);
        name = "Fuck you!";
        System.out.println(name);
    }

}
