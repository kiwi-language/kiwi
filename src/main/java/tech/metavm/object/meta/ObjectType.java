package tech.metavm.object.meta;

import org.jetbrains.annotations.Nullable;
import tech.metavm.entity.EntityType;
import tech.metavm.entity.ElementVisitor;
import tech.metavm.object.meta.rest.dto.ObjectTypeKey;
import tech.metavm.object.meta.rest.dto.TypeKey;
import tech.metavm.object.meta.rest.dto.TypeParam;

import java.util.function.Function;

@EntityType("Object类型")
public class ObjectType extends Type {

    public ObjectType() {
        super("对象",  false, false, TypeCategory.OBJECT);
        setCode("Object");
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitObjectType(this);
    }

    @Override
    public TypeKey getTypeKey() {
        return new ObjectTypeKey();
    }

    @Override
    protected boolean isAssignableFrom0(Type that) {
        return true;
    }

    @Override
    protected TypeParam getParam() {
        return null;
    }

    @Override
    public String getCanonicalName(Function<Type, java.lang.reflect.Type> getJavaType) {
        return "Any";
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof ObjectType;
    }

    @Override
    public int hashCode() {
        return ObjectType.class.hashCode();
    }

}
