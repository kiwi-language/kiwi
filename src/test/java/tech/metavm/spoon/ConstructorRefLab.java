package tech.metavm.spoon;

import java.util.function.IntFunction;

public class ConstructorRefLab {

    private int value;

    public ConstructorRefLab(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public static ConstructorRefLab foo(IntFunction<ConstructorRefLab> func) {
        return func.apply(1);
    }

    public static ConstructorRefLab test() {
        return foo(ConstructorRefLab::new);
    }

}
