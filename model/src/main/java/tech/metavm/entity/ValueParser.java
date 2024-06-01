package tech.metavm.entity;

import tech.metavm.object.type.ColumnStore;
import tech.metavm.object.type.TypeCategory;

import java.lang.reflect.Type;

public class ValueParser<T> extends PojoParser<T, ValueDef<T>> {

    public ValueParser(Class<T> entityType, Type genericType, DefContext defContext, ColumnStore columnStore) {
        super(entityType, genericType, defContext, columnStore);
    }

    @Override
    protected ValueDef<T> createDef(PojoDef<? super T> superDef) {
        return new ValueDef<>(
                javaClass,
                getJavaType(),
                superDef,
                createType(),
                defContext
        );
    }

    @Override
    protected TypeCategory getTypeCategory() {
        return TypeCategory.VALUE;
    }

}
