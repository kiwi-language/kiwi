package tech.metavm.entity;

import tech.metavm.object.instance.ModelInstanceMap;

import java.util.function.Function;

public class EntityParser<T extends Entity> extends PojoParser<T, EntityDef<T>> {

    public static <T extends Entity> EntityDef<T> parse(Class<T> entityType,
                                                        Function<Object, Long> getId,
                                                        DefMap defMap,
                                                        ModelInstanceMap modelInstanceMap) {
        return new EntityParser<>(entityType, getId, defMap, modelInstanceMap).parse();
    }

    public EntityParser(Class<T> entityType, Function<Object, Long> getId, DefMap defMap, ModelInstanceMap modelInstanceMap) {
        super(entityType, getId, defMap, modelInstanceMap);
    }

    @Override
    protected EntityDef<T> createDef() {
        return new EntityDef<>(
                null,
                javaType,
                defMap.getPojoDef(javaType.getSuperclass()),
                createType(),
                defMap
        );
    }

}
