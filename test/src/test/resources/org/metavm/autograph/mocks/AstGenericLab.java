package org.metavm.autograph.mocks;

public class AstGenericLab {

    public void test() {
        var queue = new AstQueue<Integer>();
        for (int i = 0; i < 10; i++) {
            queue.offer(i);
        }
    }

}
