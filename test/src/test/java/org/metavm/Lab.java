package org.metavm;

import java.io.IOException;
import java.io.Serializable;


public class Lab implements Serializable {

    public static final String productFile = "/Users/leen/workspace/shopping/target/shopping/Product.mvclass";
    public static final String productStatusFile = "/Users/leen/workspace/shopping/target/shopping/ProductStatus.mvclass";

    public static void main(String[] args) throws IOException {
    }

    private static void action(String s) {}

    private static void test(It it) {

    }

}

@FunctionalInterface
interface It {

    void accept(Object...args);

}