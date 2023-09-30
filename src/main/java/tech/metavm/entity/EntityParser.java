package tech.metavm.entity;

import tech.metavm.object.meta.*;

import java.lang.reflect.Type;
import java.util.List;

public class EntityParser<T extends Entity> extends PojoParser<T, EntityDef<T>> {

    public EntityParser(Class<T> entityType, Type genericType, DefMap defMap) {
        super(entityType, genericType, defMap);
    }

    @Override
    protected EntityDef<T> createDef(PojoDef<? super T> superDef) {
        return new EntityDef<>(
                javaClass,
                getJavaType(),
                superDef,
                createType(),
                defMap
        );
    }

    @Override
    protected TypeCategory getTypeCategory() {
        return TypeCategory.CLASS;
    }
}
