package tech.metavm.autograph.mocks;

public class GenericOverrideFoo {

    <T> void test() {}

}

class GenericOverrideSub extends GenericOverrideFoo {

    void test() {

    }

}
