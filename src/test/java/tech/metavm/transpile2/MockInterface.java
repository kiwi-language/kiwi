package tech.metavm.transpile2;

public interface MockInterface {

    void foo();

    int bar();

    default int qux() {
        return 1;
    }

}
