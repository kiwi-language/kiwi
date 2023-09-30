package tech.metavm.autograph;

import tech.metavm.autograph.mocks.Foo;

import java.util.Arrays;

public class AutographLab {

    public static void main(String[] args) {
        var fooType = TranspileTestTools.getPsiClass(Foo.class);
        var superTypes = fooType.getSuperTypes();
        var objectType = TranspileUtil.getPsiElementFactory().createTypeByFQClassName(Object.class.getName());
        var objectClass = objectType.resolve();
        System.out.println(Arrays.toString(superTypes));
    }

}