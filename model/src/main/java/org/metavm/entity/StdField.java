package org.metavm.entity;

import org.metavm.api.Index;
import org.metavm.entity.natives.HybridValueHolder;
import org.metavm.entity.natives.ValueHolder;
import org.metavm.entity.natives.ValueHolderOwner;
import org.metavm.object.type.Field;

public enum StdField implements ValueHolderOwner<Field> {

    enumName(Enum.class, "name"),
    enumOrdinal(Enum.class, "ordinal"),
    indexName(Index.class, "name"),
    byteValue(Byte.class, "value"),
    shortValue(Short.class, "value"),
    integerValue(Integer.class, "value"),
    longValue(Long.class, "value"),
    floatValue(Float.class, "value"),
    doubleValue(Double.class, "value"),
    characterValue(Character.class, "value"),
    booleanValue(Boolean.class, "value"),
    ;

    private final Class<?> javaClass;
    private final String fieldName;
    private ValueHolder<Field> fieldHolder = new HybridValueHolder<>();

    StdField(Class<?> javaClass, String fieldName) {
        this.javaClass = javaClass;
        this.fieldName = fieldName;
    }

    public Field get() {
        return fieldHolder.get();
    }

    private void set(Field field) {
        fieldHolder.set(field);
    }

    public void setLocal(Field field) {
        fieldHolder.setLocal(field);
    }

    public void init(DefContext defContext, boolean local) {
        var klass = defContext.getKlass(javaClass);
        var field = klass.getFieldByName(fieldName);
        if(local)
            setLocal(field);
        else
            set(field);
    }

    public void setValueHolder(ValueHolder<Field> methodHolder) {
        this.fieldHolder = methodHolder;
    }

    @Override
    public ValueHolder<Field> getValueHolder() {
        return fieldHolder;
    }

    public static void initialize(DefContext defContext, boolean local) {
        for (StdField value : values()) {
            value.init(defContext, local);
        }
    }

}
