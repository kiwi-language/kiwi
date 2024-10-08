package org.metavm.entity;

import org.metavm.object.type.ColumnStore;
import org.metavm.object.type.TypeCategory;

import java.lang.reflect.Type;

public class EntityParser<T> extends PojoParser<T, EntityDef<T>> {

    public EntityParser(Class<T> entityType, Type genericType, SystemDefContext defContext, ColumnStore columnStore) {
        super(entityType, genericType, defContext, columnStore);
    }

    @Override
    protected EntityDef<T> createDef(PojoDef<? super T> superDef) {
        return new EntityDef<>(
                javaClass,
                getJavaType(),
                superDef,
                createKlass(),
                defContext
        );
    }

    @Override
    protected TypeCategory getTypeCategory() {
        return TypeCategory.CLASS;
    }

}
