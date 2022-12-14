package tech.metavm.entity;

import tech.metavm.object.meta.ClassType;
import tech.metavm.object.meta.TypeFactory;

import java.lang.reflect.Type;

public class ValueParser<T> extends PojoParser<T, ValueDef<T>> {

    public static <T>ValueDef<T> parse(Class<T> entityType, Type genericType, DefMap defMap) {
        return new ValueParser<T>(entityType, genericType, defMap).parse();
    }

    public ValueParser(Class<T> entityType, Type genericType, DefMap defMap) {
        super(entityType, genericType, defMap);
    }

    @Override
    protected ValueDef<T> createDef(PojoDef<? super T> superDef) {
        return new ValueDef<>(
                javaType,
                getGenericType(),
                superDef,
                createType(),
                defMap
        );
    }

    @Override
    protected ClassType createType(TypeFactory typeFactory, String name, String code, ClassType superType) {
        return typeFactory.createValueClass(name, code, superType);
    }
}
