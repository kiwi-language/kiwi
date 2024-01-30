package tech.metavm.autograph.mocks;

public class FooLoopFoo {

    public int test() {
        for (int i = 0; i < 10; i++) {

        }
        for (int i = 0, j = 0; i < 10 && j < 10; i++, j++) {}
        var i = 2;
        {
            return i;
        }
    }

}
