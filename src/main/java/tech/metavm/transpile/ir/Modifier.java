package tech.metavm.transpile.ir;

import tech.metavm.util.NncUtils;

import java.util.Arrays;
import java.util.List;

public enum Modifier {

    PUBLIC(java.lang.reflect.Modifier.PUBLIC),
    PROTECTED(java.lang.reflect.Modifier.PROTECTED),
    PRIVATE(java.lang.reflect.Modifier.PRIVATE),
    STATIC(java.lang.reflect.Modifier.STATIC),
    DEFAULT(0),
    ABSTRACT(java.lang.reflect.Modifier.ABSTRACT),
    FINAL(java.lang.reflect.Modifier.FINAL);

    private final int javaModifierBit;

    Modifier(int javaModifierBit) {
        this.javaModifierBit = javaModifierBit;
    }

    public static List<Modifier> getByJavaModifiers(int javaModifiers) {
        return NncUtils.filter(
                Arrays.asList(values()),
                v -> (v.javaModifierBit & javaModifiers) != 0
        );
    }

    public static boolean isImmutable(int javaModifierBit) {
        return getByJavaModifiers(javaModifierBit).contains(FINAL);
    }

}
