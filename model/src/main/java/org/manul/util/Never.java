package org.manul.util;

public class Never {

    private Never() {
        throw new RuntimeException("Can not instantiate");
    }

}
