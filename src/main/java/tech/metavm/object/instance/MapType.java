package tech.metavm.object.instance;

import tech.metavm.entity.EntityField;
import tech.metavm.entity.EntityType;
import tech.metavm.object.meta.Type;
import tech.metavm.object.meta.TypeCategory;

import java.util.function.Function;

@EntityType("词典类型")
public class MapType extends Type {

    @EntityField("键类型")
    private Type keyType;
    @EntityField("值类型")
    private Type valueType;

    public MapType(String name, boolean anonymous, boolean ephemeral, TypeCategory category) {
        super(name, anonymous, ephemeral, category);
    }

    @Override
    public boolean isAssignableFrom(Type that) {
        return false;
    }

    @Override
    protected Object getParam() {
        return null;
    }

    @Override
    public String getCanonicalName(Function<Type, java.lang.reflect.Type> getJavaType) {
        return null;
    }

    public Type getKeyType() {
        return keyType;
    }

    public Type getValueType() {
        return valueType;
    }

}
