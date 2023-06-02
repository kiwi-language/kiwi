package tech.metavm.transpile.ir;

import tech.metavm.util.NncUtils;

public enum IRPrimitiveKind {
    BYTE(byte.class),
    SHORT(short.class),
    INT(int.class),
    LONG(long.class),
    FLOAT(float.class),
    DOUBLE(double.class),
    CHAR(char.class),
    BOOLEAN(boolean.class),
    VOID(void.class)

    ;


    private final String name;
    private final Class<?> klass;

    IRPrimitiveKind(Class<?> klass) {
        this.klass = klass;
        name = klass.getSimpleName();
    }

    public String getName() {
        return name;
    }

    public static IRPrimitiveKind getByClass(Class<?> klass) {
        return NncUtils.findRequired(values(), v -> v.klass == klass);
    }

    public Class<?> getKlass() {
        return klass;
    }
}
