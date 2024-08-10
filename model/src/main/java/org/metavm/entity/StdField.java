package org.metavm.entity;

import org.metavm.entity.natives.DirectValueHolder;
import org.metavm.entity.natives.ValueHolder;
import org.metavm.entity.natives.ValueHolderOwner;
import org.metavm.object.type.Field;

public enum StdField implements ValueHolderOwner<Field> {

    enumName(Enum.class, "name"),
    enumOrdinal(Enum.class, "ordinal"),

    ;

    private final Class<?> javaClass;
    private final String fieldName;
    private ValueHolder<Field> methodHolder = new DirectValueHolder<>();

    StdField(Class<?> javaClass, String fieldName) {
        this.javaClass = javaClass;
        this.fieldName = fieldName;
    }

    public Field get() {
        return methodHolder.get();
    }

    private void set(Field field) {
        methodHolder.set(field);
    }

    public void init(SystemDefContext defContext) {
        var klass = defContext.getKlass(javaClass);
        var field = klass.getFieldByCode(fieldName);
        set(field);
    }

    public void setValueHolder(ValueHolder<Field> methodHolder) {
        this.methodHolder = methodHolder;
    }

    public static void initialize(SystemDefContext defContext) {
        for (StdField value : values()) {
            value.init(defContext);
        }
    }

}
