package org.metavm.entity;

import lombok.extern.slf4j.Slf4j;
import org.metavm.object.instance.core.*;
import org.metavm.object.type.*;
import org.metavm.util.*;

import javax.annotation.Nullable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.*;

@Slf4j
public abstract class DefContext implements IInstanceContext, TypeRegistry {

    public DefContext() {
    }

    public abstract KlassDef<?> tryGetDef(TypeDef typeDef);

    public KlassDef<?> getDef(TypeDef typeDef) {
        return Objects.requireNonNull(tryGetDef(typeDef), "Can not find def for: " + typeDef);
    }

    public abstract KlassDef<?> getDef(Type javaType);

    public abstract @Nullable KlassDef<?> getDefIfPresent(Type javaType);

    public abstract Collection<KlassDef<?>> getAllDefList();

    public abstract Id getModelId(Object o);

    public  <T> KlassDef<T> getDef(Class<T> klass) {
        //noinspection unchecked
        return (KlassDef<T>) getDef((Type) klass);
    }

    public org.metavm.object.type.Type getNullableType(Type javaType) {
        var type = getType(javaType);
        if(!ReflectionUtils.isPrimitiveType(javaType))
            type = Types.getNullableType(type);
        return type;
    }

    public org.metavm.object.type.Type getType(Type javaType) {
        var type = getPrimitiveType(javaType);
        if (type != null)
            return type;
        if (javaType instanceof Class<?> k && (Value.class == k || EntityReference.class == k))
            return AnyType.instance;
        if (BiUnion.isNullable(javaType))
            return Types.getNullableType(getType(BiUnion.getUnderlyingType(javaType)));
        if (javaType instanceof ParameterizedType pType) {
            var rawClass = (Class<?>) pType.getRawType();
            return new KlassType(
                    pType.getOwnerType() != null ? getDef(pType.getOwnerType()).getType() : null,
                    getDef(rawClass).getType().getKlass(), Utils.map(pType.getActualTypeArguments(), this::getType)
            );
        } else if (javaType instanceof java.lang.reflect.TypeVariable<?> tv) {
            if (tv.getGenericDeclaration() instanceof Class<?> declaringClass)
                return getKlass(declaringClass).getTypeParameterByName(tv.getName()).getType();
            else
                throw new IllegalArgumentException("Cannot resolve type variable '" + tv
                        + "'. Only class type parameters are supported");
        } else
            return getDef(javaType).getType();
    }

    public abstract boolean containsDef(TypeDef typeDef);

    protected void checkJavaType(Type javaType) {
        if (javaType instanceof WildcardType && javaType instanceof java.lang.reflect.TypeVariable<?>) {
            throw new InternalException("Can not get def for java type '" + javaType.getTypeName() + "', " +
                    "Because it's either a wildcard type or a type variable");
        }
    }

    public Klass getKlass(Class<?> javaClass) {
        return getDef(javaClass).getTypeDef();
    }

    public Klass tryGetKlass(Class<?> javaClass) {
        var def = getDefIfPresent(javaClass);
        return def != null ? def.getTypeDef() : null;
    }

    public org.metavm.object.type.Type getType(Class<?> javaClass) {
        return getType((Type) javaClass);
    }

    public abstract Collection<Entity> entities();


    @Override
    public <T extends Entity> List<T> query(EntityIndexQuery<T> query) {
        return List.of();
    }

    @Override
    public long count(EntityIndexQuery<?> query) {
        return 0;
    }

    public static @javax.annotation.Nullable org.metavm.object.type.Type getPrimitiveType(java.lang.reflect.Type javaType) {
        if (javaType instanceof Class<?> javaClass) {
            if (javaClass == Object.class)
                return AnyType.instance;
            if (javaClass == Never.class)
                return NeverType.instance;
//            if (javaClass == String.class)
//                return PrimitiveType.stringType;
            if (javaClass == long.class)
                return PrimitiveType.longType;
            if( javaClass == int.class)
                return PrimitiveType.intType;
            if (javaClass == short.class)
                return PrimitiveType.shortType;
            if (javaClass == byte.class)
                return PrimitiveType.byteType;
            if (javaClass == char.class)
                return PrimitiveType.charType;
            if (javaClass == boolean.class)
                return PrimitiveType.booleanType;
            if (javaClass == double.class)
                return PrimitiveType.doubleType;
            if (javaClass == float.class)
                return PrimitiveType.floatType;
            if (javaClass == void.class)
                return PrimitiveType.voidType;
            if (javaClass == Password.class)
                return PrimitiveType.passwordType;
            if (javaClass == Date.class)
                return PrimitiveType.timeType;
            if (javaClass == Null.class)
                return NullType.instance;
        }
        return null;
    }

    @Override
    public boolean isMigrating() {
        return false;
    }

    @Override
    public void dumpContext() {
        for (Entity entity : entities()) {
            log.trace("Entity {}-{}", entity.getClass().getName(), entity.getId());
        }
    }
}
