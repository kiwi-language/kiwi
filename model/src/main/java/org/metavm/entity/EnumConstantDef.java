package org.metavm.entity;

import org.metavm.api.EnumConstant;
import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.instance.core.Id;
import org.metavm.object.type.EnumConstantRT;
import org.metavm.util.ReflectionUtils;

import java.util.function.Function;

public class EnumConstantDef<T extends Enum<?>> {
    private final String name;
    private final int ordinal;
    private final T value;
    private EnumConstantRT enumConstant;
    private ClassInstance instance;
    private final EnumDef<T> enumDef;

    public EnumConstantDef(T value, EnumDef<T> enumDef, Function<Object, Id> getId) {
        java.lang.reflect.Field enumField = ReflectionUtils.getField(value.getClass(), value.name());
        this.enumDef = enumDef;
        this.enumConstant = enumDef.createEnumConstant(value, enumField, getId);
        this.instance = enumConstant.getInstance();
        EnumConstant annotation = enumField.getAnnotation(EnumConstant.class);
        this.name = annotation != null && !annotation.value().isEmpty() ? annotation.value() : value.name();
        ordinal = value.ordinal();
        this.value = value;
        enumDef.addEnumConstantDef(this);
    }

    public EnumConstantDef(T value, EnumDef<T> enumDef, ClassInstance instance) {
        java.lang.reflect.Field enumField = ReflectionUtils.getField(value.getClass(), value.name());
        this.enumDef = enumDef;
        this.enumConstant = enumDef.createEnumConstant(instance);
        this.instance = enumConstant.getInstance();
        EnumConstant annotation = enumField.getAnnotation(EnumConstant.class);
        this.name = annotation != null && !annotation.value().isEmpty() ? annotation.value() : value.name();
        ordinal = value.ordinal();
        this.value = value;
        enumDef.addEnumConstantDef(this);
    }

    public EnumConstantRT getEnumConstant() {
        return enumConstant;
    }

    public ClassInstance getInstance() {
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

    public void setEnumConstant(ClassInstance instance) {
        this.instance = instance;
        this.enumConstant = new EnumConstantRT(instance);
    }

}
