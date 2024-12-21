package org.metavm.entity;

import org.metavm.api.ValueObject;
import org.metavm.flow.*;
import org.metavm.object.type.ColumnStore;
import org.metavm.object.type.TypeCategory;
import org.metavm.util.NncUtils;
import org.metavm.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.RecordComponent;
import java.lang.reflect.Type;
import java.util.List;

public class RecordParser<T extends Record> extends PojoParser<T, RecordDef<T>> {

    public RecordParser(Class<T> entityType, Type genericType, SystemDefContext defContext, ColumnStore columnStore) {
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
                createKlass(),
                defContext
        );
    }

    @Override
    protected void declareApiMethods() {
        MethodBuilder.newBuilder(get().getKlass(), javaClass.getSimpleName())
                .isConstructor(true)
                .parameters(NncUtils.map(
                        javaClass.getRecordComponents(),
                        c -> new NameAndType(c.getName(), defContext.getType(c.getGenericType()))
                ))
                .returnType(get().getType())
                .build();
        for (RecordComponent recordComponent : javaClass.getRecordComponents()) {
            MethodBuilder.newBuilder(get().getKlass(), recordComponent.getName())
                    .returnType(defContext.getType(recordComponent.getGenericType()))
                    .build();
        }
    }

    @Override
    public void generateDefinition() {
        super.generateDefinition();
        if (isSystemAPI()) {
            var klass = get().getKlass();
            var constructor = NncUtils.findRequired(klass.getMethods(), Method::isConstructor);
            {
                var code = constructor.getCode();
                code.setStrictEphemeral(true);
                int i = 0;
                for (var field : klass.getFields()) {
                   if(!field.isStatic()) {
                       Nodes.this_(code);
                       Nodes.argument(constructor, i++);
                       Nodes.setField(field.getRef(), code);
                   }
                }
                Nodes.this_(code);
                Nodes.ret(code);
            }
            for (org.metavm.object.type.Field field : klass.getFields()) {
                var accessor = klass.getMethodByName(field.getName());
                var code = accessor.getCode();
                code.setStrictEphemeral(true);
                Nodes.thisField(field.getRef(), code);
                Nodes.ret(code);
            }
        }
        get().klass.emitCode();
    }

    @Override
    protected TypeCategory getTypeCategory() {
        return ValueObject.class.isAssignableFrom(javaClass) ? TypeCategory.VALUE : TypeCategory.CLASS;
    }

}
