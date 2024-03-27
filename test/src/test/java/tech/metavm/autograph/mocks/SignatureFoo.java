package tech.metavm.autograph.mocks;

import java.util.List;

public class SignatureFoo {

    public <T> void add(List<? super T> list, T element) {
        list.add(element);
    }

    public void test(Object o) {}

}
