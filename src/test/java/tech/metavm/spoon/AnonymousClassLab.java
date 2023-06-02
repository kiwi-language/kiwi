package tech.metavm.spoon;

public class AnonymousClassLab {

    public Object test() {
        return new Object() {
            public void foo() {
                bar();
            }
        };
    }

    private void bar() {

    }

}
