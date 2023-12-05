package tech.metavm.object.type;

import tech.metavm.entity.EntityType;
import tech.metavm.entity.ElementVisitor;
import tech.metavm.object.type.rest.dto.NothingTypeKey;
import tech.metavm.object.type.rest.dto.TypeKey;
import tech.metavm.object.type.rest.dto.TypeParam;

import java.util.function.Function;

@EntityType("NothingType")
public class NothingType extends Type {

    public NothingType() {
        super("不可能", false, true, TypeCategory.NOTHING);
        setCode("Nothing");
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitNothingType(this);
    }

    @Override
    public TypeKey getTypeKey() {
        return new NothingTypeKey();
    }

    @Override
    protected boolean isAssignableFrom0(Type that) {
        return false;
    }

    @Override
    protected TypeParam getParam() {
        return null;
    }

    @Override
    public String getKey(Function<Type, java.lang.reflect.Type> getJavaType) {
        return "Nothing";
    }
}
