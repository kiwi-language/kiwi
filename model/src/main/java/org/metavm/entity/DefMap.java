package org.metavm.entity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;

public interface DefMap {

    Logger logger = LoggerFactory.getLogger(DefMap.class);

//    ModelDef<?> getDef(Type javaType);

//    default Mapper<?, ?> getMapper(Type javaType) {
//        return getMapper(javaType, ResolutionStage.INIT);
//    }

//    Mapper<?, ?> getMapper(Type javaType, ResolutionStage stage);

    boolean containsDef(Type javaType);

    void preAddDef(KlassDef<?> def);

    void addDef(KlassDef<?> def);

    void afterDefInitialized(KlassDef<?> def);

    boolean containsJavaType(Type javaType);

//    default <T> PojoDef<T> getPojoDef(TypeReference<T> typeRef) {
//        return new TypeReference<PojoDef<T>>() {}.cast(
//                getDef(typeRef.getGenericType())
//        );
//    }
//
//    default <T> PojoDef<T> getPojoDef(Type type) {
//        return new TypeReference<PojoDef<T>>() {}.cast(
//                getDef(type)
//        );
//    }
//
//    default <T> PojoDef<T> getPojoDef(Class<T> klass) {
//        return new TypeReference<PojoDef<T>>() {}.cast(
//                getDef(klass)
//        );
//    }

//    default <T extends Entity> EntityDef<T> getEntityDef(TypeReference<T> typeReference) {
//        return getEntityDef(typeReference.getType());
//    }
//
//    default <T extends Entity> EntityDef<T> getEntityDef(Class<T> klass) {
//        return new TypeReference<EntityDef<T>>() {}.cast(
//                getDef(klass)
//        );
//    }

//    default <T> ValueDef<T> getValueDef(Class<T> klass) {
//        return new TypeReference<ValueDef<T>>() {}.cast(
//                getDef(klass)
//        );
//    }

//    default org.metavm.object.type.Type getType(Class<?> javaClass) {
//        return getDef(javaClass).getTypeDef().getType();
//    }

//    default org.metavm.object.type.Type getType(Type javaType) {
//        return getDef(javaType).getTypeDef().getType();
//    }

}
