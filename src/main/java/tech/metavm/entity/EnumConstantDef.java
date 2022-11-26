package tech.metavm.entity;

import tech.metavm.object.instance.Instance;
import tech.metavm.object.meta.EnumConstantRT;
import tech.metavm.object.meta.Type;
import tech.metavm.util.ReflectUtils;

public class EnumConstantDef<T extends Enum<?>> {
    private final String name;
    private final int ordinal;
    private final T value;
    private final EnumConstantRT enumConstant;
    private final Instance instance;

    public EnumConstantDef(T value, EnumDef<T> enumDef, EnumConstantRT enumConstant, Instance instance) {
        java.lang.reflect.Field enumField = ReflectUtils.getField(value.getClass(), value.name());
        this.instance = instance != null ? instance : enumDef.createInstance(value);
        EnumConstant annotation = enumField.getAnnotation(EnumConstant.class);
        this.name = annotation != null ? annotation.value() : value.name();
        ordinal = value.ordinal();
        this.value = value;
        enumDef.addEnumConstantDef(this);
        this.enumConstant = createEnumConstant(enumDef.getType(), enumConstant);
    }

    public EnumConstantRT createEnumConstant(Type enumType, EnumConstantRT enumConstant) {
        if(enumConstant == null) {
            enumConstant = new EnumConstantRT(enumType, name, ordinal);
        }
        else {
            enumConstant.setName(name);
            enumConstant.setOrdinal(ordinal);
        }
        return enumConstant;
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
