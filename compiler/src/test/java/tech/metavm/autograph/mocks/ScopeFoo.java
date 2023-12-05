package tech.metavm.autograph.mocks;

public class ScopeFoo {

    public Integer test(Integer t) {
        int p = 0;
        int q = 1;
        while (t-- > 0) {
            p *= q++;
        }
        return p ^ this.hashCode();
    }

}
