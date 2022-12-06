package tech.metavm.entity;

import tech.metavm.object.instance.ModelInstanceMap;

import java.util.function.Function;

public class ValueParser<T> extends PojoParser<T, ValueDef<T>> {

    public static <T>ValueDef<T> parse(Class<T> entityType, Function<Object, Long> getId, DefMap defMap, ModelInstanceMap modelInstanceMap) {
        return new ValueParser<T>(entityType, getId, defMap, modelInstanceMap).parse();
    }

    public ValueParser(Class<T> entityType, Function<Object, Long> getId, DefMap defMap, ModelInstanceMap modelInstanceMap) {
        super(entityType, getId, defMap, modelInstanceMap);
    }

    @Override
    protected ValueDef<T> createDef() {
        return new ValueDef<>(
                null,
                javaType,
                defMap.getPojoDef(javaType.getSuperclass()),
                createType(),
                defMap
        );
    }
}
