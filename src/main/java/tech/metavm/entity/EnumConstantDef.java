package tech.metavm.entity;

import tech.metavm.object.instance.core.ClassInstance;
import tech.metavm.object.instance.core.Instance;
import tech.metavm.object.type.EnumConstantRT;
import tech.metavm.util.ReflectUtils;

public class EnumConstantDef<T extends Enum<?>> {
    private final String name;
    private final int ordinal;
    private final T value;
    private final EnumConstantRT enumConstant;
    private final ClassInstance instance;
    private final EnumDef<T> enumDef;

    public EnumConstantDef(T value, EnumDef<T> enumDef) {
        java.lang.reflect.Field enumField = ReflectUtils.getField(value.getClass(), value.name());
        this.enumDef = enumDef;
        this.enumConstant = enumDef.createEnumConstant(value, enumField);
        this.instance = enumConstant.getInstance();
        EnumConstant annotation = enumField.getAnnotation(EnumConstant.class);
        this.name = annotation != null ? annotation.value() : value.name();
        ordinal = value.ordinal();
        this.value = value;
        enumDef.addEnumConstantDef(this);
    }

    public EnumConstantRT getEnumConstant() {
        return enumConstant;
    }

    public Instance getInstance() {
        return instance;
    }

    public String getName() {
        return name;
    }

    public T getValue() {
        return value;
    }

    public Long getId() {
        return enumConstant.getId();
    }
}
