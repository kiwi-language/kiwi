package tech.metavm.entity;

import tech.metavm.object.type.*;

import java.lang.reflect.Type;

public class EntityParser<T extends Entity> extends PojoParser<T, EntityDef<T>> {

    public EntityParser(Class<T> entityType, Type genericType, DefContext defContext, ColumnStore columnStore) {
        super(entityType, genericType, defContext, columnStore);
    }

    @Override
    protected EntityDef<T> createDef(PojoDef<? super T> superDef) {
        return new EntityDef<>(
                javaClass,
                getJavaType(),
                superDef,
                createType(),
                defContext
        );
    }

    @Override
    protected TypeCategory getTypeCategory() {
        return TypeCategory.CLASS;
    }
}
