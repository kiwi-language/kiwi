package tech.metavm.entity;

import tech.metavm.object.meta.*;

import java.lang.reflect.Type;
import java.util.List;

public class ValueParser<T> extends PojoParser<T, ValueDef<T>> {

    public ValueParser(Class<T> entityType, Type genericType, DefMap defMap) {
        super(entityType, genericType, defMap);
    }

    @Override
    protected ValueDef<T> createDef(PojoDef<? super T> superDef) {
        return new ValueDef<>(
                javaClass,
                getJavaType(),
                superDef,
                createType(),
                defMap
        );
    }

    @Override
    protected TypeCategory getTypeCategory() {
        return TypeCategory.VALUE;
    }

}
