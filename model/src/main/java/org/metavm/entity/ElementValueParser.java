package org.metavm.entity;

import org.metavm.object.instance.core.Value;
import org.metavm.object.type.ColumnStore;
import org.metavm.object.type.TypeCategory;

import java.lang.reflect.Type;

public class ElementValueParser<T extends Value> extends PojoParser<T, ElementValueDef<T>> {

    public ElementValueParser(Class<T> entityType, Type genericType, SystemDefContext defContext, ColumnStore columnStore) {
        super(entityType, genericType, defContext, columnStore);
    }

    @Override
    protected ElementValueDef<T> createDef(PojoDef<? super T> parentDef) {
        return new ElementValueDef<>(
                javaClass,
                getJavaType(),
                parentDef,
                createKlass(),
                defContext
        );
    }

    @Override
    protected TypeCategory getTypeCategory() {
        return TypeCategory.VALUE;
    }

}
