package tech.metavm.entity;

import tech.metavm.util.TypeReference;

import java.lang.reflect.Type;

public interface DefMap {

    ModelDef<?, ?> getDef(Type javaType);

    ModelDef<?, ?> getDef(tech.metavm.object.meta.Type type);

    void addDef(ModelDef<?, ?> def);

    default <T> PojoDef<T> getPojoDef(TypeReference<T> typeRef) {
        return new TypeReference<PojoDef<T>>() {}.cast(
                getDef(typeRef.getGenericType())
        );
    }

    default <T> PojoDef<T> getPojoDef(Class<T> klass) {
        return new TypeReference<PojoDef<T>>() {}.cast(
                getDef(klass)
        );
    }

    default <T extends Entity> EntityDef<T> getEntityDef(Class<T> klass) {
        return new TypeReference<EntityDef<T>>() {}.cast(
                getDef(klass)
        );
    }

    default <T> ValueDef<T> getValueDef(Class<T> klass) {
        return new TypeReference<ValueDef<T>>() {}.cast(
                getDef(klass)
        );
    }

}
