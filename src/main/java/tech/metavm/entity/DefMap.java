package tech.metavm.entity;

import tech.metavm.util.RuntimeGeneric;
import tech.metavm.util.TypeReference;

import java.lang.reflect.Type;

public interface DefMap {

    ModelDef<?, ?> getDef(Type javaType);

    boolean containsDef(Type javaType);

    default ModelDef<?,?> getDefByModel(Object model) {
        if(model instanceof RuntimeGeneric runtimeGeneric) {
            return getDef(runtimeGeneric.getGenericType());
        }
        else {
            return getDef(model.getClass());
        }
    }

    tech.metavm.object.meta.Type internType(tech.metavm.object.meta.Type type);

    ModelDef<?, ?> getDef(tech.metavm.object.meta.Type type);

    void preAddDef(ModelDef<?,?> def);

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

    default <T extends Entity> EntityDef<T> getEntityDef(TypeReference<T> typeReference) {
        return getEntityDef(typeReference.getType());
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

    default tech.metavm.object.meta.Type getType(Class<?> javaClass) {
        return getDef(javaClass).getType();
    }

    default tech.metavm.object.meta.Type getType(Type javaType) {
        return getDef(javaType).getType();
    }

}
