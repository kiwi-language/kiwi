package tech.metavm.spoon;

public interface IFoo {

    default void bar() {
        System.out.println("bar");
    }

    void baz();

}
