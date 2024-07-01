package org.metavm.entity;

import org.metavm.object.type.ResolutionStage;
import org.metavm.object.type.TypeDef;
import org.metavm.util.RuntimeGeneric;
import org.metavm.util.TypeReference;

import java.lang.reflect.Type;

public interface DefMap {

    ModelDef<?> getDef(Type javaType);

    ModelDef<?> getDef(TypeDef typeDef);

    default Mapper<?, ?> getMapper(Type javaType) {
        return getMapper(javaType, ResolutionStage.INIT);
    }

    Mapper<?, ?> getMapper(Type javaType, ResolutionStage stage);

    Mapper<?, ?> getMapper(org.metavm.object.type.Type type);

    boolean containsDef(Type javaType);

    boolean containsDef(TypeDef typeDef);

    default Mapper<?, ?> getMapperByEntity(Object entity) {
        if(entity instanceof RuntimeGeneric runtimeGeneric)
            return getMapper(runtimeGeneric.getGenericType(), ResolutionStage.DEFINITION);
        else
            return getMapper(entity.getClass(), ResolutionStage.DEFINITION);
    }

    org.metavm.object.type.Type internType(org.metavm.object.type.Type type);

    void preAddDef(ModelDef<?> def);

    void addDef(ModelDef<?> def);

    void afterDefInitialized(ModelDef<?> def);

    boolean containsJavaType(Type javaType);

    default <T> PojoDef<T> getPojoDef(TypeReference<T> typeRef) {
        return new TypeReference<PojoDef<T>>() {}.cast(
                getDef(typeRef.getGenericType())
        );
    }

    default <T> PojoDef<T> getPojoDef(Type type) {
        return new TypeReference<PojoDef<T>>() {}.cast(
                getDef(type)
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

    default org.metavm.object.type.Type getType(Class<?> javaClass) {
        return getDef(javaClass).getTypeDef().getType();
    }

    default org.metavm.object.type.Type getType(Type javaType) {
        return getDef(javaType).getTypeDef().getType();
    }

}
