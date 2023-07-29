package tech.metavm.autograph.mocks;

public class ReachingDefFoo {

    public void test(String name) {
        long l = 1;
        System.out.println(name + ": " + l);
    }

}
