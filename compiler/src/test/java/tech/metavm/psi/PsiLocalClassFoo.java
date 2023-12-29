package tech.metavm.psi;

public class PsiLocalClassFoo {

    public <E> Object test(E e) {
        class Foo<T> {
            final T t;
            final E e;

            Foo(T t, E e) {
                this.t = t;
                this.e = e;
            }
        }
        return new Foo<>("Hello", e);
    }

}
