package tech.metavm.entity;

import tech.metavm.object.instance.IInstance;
import tech.metavm.object.instance.ModelMap;

import java.util.function.Function;

public class ValueParser<T> extends PojoParser<T, ValueDef<T>> {

    public static <T>ValueDef<T> parse(Class<T> entityType, Function<Object, IInstance> getInstance, DefMap defMap, ModelMap modelMap) {
        return new ValueParser<T>(entityType, getInstance, defMap, modelMap).parse();
    }

    public ValueParser(Class<T> entityType, Function<Object, IInstance> getInstance, DefMap defMap, ModelMap modelMap) {
        super(entityType, getInstance, defMap, modelMap);
    }

    @Override
    protected ValueDef<T> createDef() {
        return new ValueDef<>(
                null,
                entityType,
                defMap.getPojoDef(entityType.getSuperclass()),
                type
        );
    }
}
