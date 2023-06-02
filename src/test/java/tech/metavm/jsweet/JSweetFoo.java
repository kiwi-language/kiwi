package tech.metavm.jsweet;

public class JSweetFoo {

    public void test() {
        System.out.println(foo(""));
    }

    public <E> E foo(E e) {
        return e;
    }

}
