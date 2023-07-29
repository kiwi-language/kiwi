package tech.metavm.autograph.mocks;

import java.util.List;

public class ScopeFoo {

    public int test(int t) {
        int p = 0;
        int q = 1;
        while (t-- > 0) {
            p *= q++;
        }
        return p;
    }

}
