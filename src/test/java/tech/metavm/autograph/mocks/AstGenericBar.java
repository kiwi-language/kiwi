package tech.metavm.autograph.mocks;

public class AstGenericBar {

    void test() {
        AstGenericFoo<String> foo1 = new AstGenericFoo<>("Hello");
        AstGenericFoo<String> foo2 = new AstGenericFoo<>("World");
        foo1.copyValue(foo2);
    }

}
