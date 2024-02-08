package tech.metavm.util;

public class Never {

    private Never() {
        throw new RuntimeException("Can not instantiate");
    }

}
