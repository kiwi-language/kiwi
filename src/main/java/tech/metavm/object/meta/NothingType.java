package tech.metavm.object.meta;

import tech.metavm.entity.EntityType;
import tech.metavm.object.meta.rest.dto.TypeParam;

import java.util.function.Function;

@EntityType("NothingType")
public class NothingType extends Type {

    public NothingType() {
        super("不可能", false, true, TypeCategory.NOTHING);
        setCode("Nothing");
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
    public String getCanonicalName(Function<Type, java.lang.reflect.Type> getJavaType) {
        return "Nothing";
    }
}
