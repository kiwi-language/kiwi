package tech.metavm.transpile2;

import tech.metavm.spoon.AnonymousClassLab;

public class TranspilerLab {

    public static void main(String[] args) {
        var type = TranspileTestHelper.getCtClass(AnonymousClassLab.class);
        System.out.println(type);
    }

}
