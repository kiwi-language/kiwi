package org.metavm.entity;

import org.metavm.object.type.ColumnStore;
import org.metavm.object.type.TypeCategory;
import org.metavm.util.NncUtils;
import org.metavm.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.List;

public class RecordParser<T extends Record> extends PojoParser<T, RecordDef<T>> {

    public RecordParser(Class<T> entityType, Type genericType, DefContext defContext, ColumnStore columnStore) {
        super(entityType, genericType, defContext, columnStore);
    }

    @Override
    protected List<Field> getPropertyFields() {
        return NncUtils.map(javaClass.getRecordComponents(), ReflectionUtils::getField);
    }

    @Override
    protected RecordDef<T> createDef(PojoDef<? super T> parentDef) {
        return new RecordDef<>(
                javaClass,
                getJavaType(),
                parentDef,
                createType(),
                defContext
        );
    }

    @Override
    protected TypeCategory getTypeCategory() {
        return TypeCategory.CLASS;
    }
}
