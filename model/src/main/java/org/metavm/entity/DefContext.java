package org.metavm.entity;

import lombok.extern.slf4j.Slf4j;
import org.metavm.object.instance.core.IInstanceContext;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.instance.core.Value;
import org.metavm.object.type.*;
import org.metavm.util.*;

import javax.annotation.Nullable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.metavm.object.type.ResolutionStage.DEFINITION;
import static org.metavm.object.type.ResolutionStage.INIT;

@Slf4j
public abstract class DefContext implements IInstanceContext, TypeRegistry {

    public static final Map<Class<?>, Class<?>> BOX_CLASS_MAP = Map.ofEntries(
            Map.entry(Byte.class, Integer.class),
            Map.entry(Short.class, Integer.class)
    );

    public DefContext() {
    }

    public abstract KlassDef<?> tryGetDef(TypeDef typeDef);

    public KlassDef<?> getDef(TypeDef typeDef) {
        return Objects.requireNonNull(tryGetDef(typeDef), "Can not find def for: " + typeDef);
    }

    public abstract KlassDef<?> getDef(Type javaType, ResolutionStage stage);

    public abstract @Nullable KlassDef<?> getDefIfPresent(Type javaType);

    public abstract Collection<KlassDef<?>> getAllDefList();

    public  <T> KlassDef<T> getDef(Class<T> klass) {
        //noinspection unchecked
        return (KlassDef<T>) getDef((Type) klass);
    }

    public KlassDef<?> getDef(Type javaType) {
        return getDef(javaType, DEFINITION);
    }

    public org.metavm.object.type.Type getNullableType(Type javaType) {
        var type = getType(javaType);
        if(!ReflectionUtils.isPrimitiveType(javaType))
            type = Types.getNullableType(type);
        return type;
    }

    public org.metavm.object.type.Type getType(Type javaType) {
        var type = Types.getPrimitiveType(javaType);
        if (type != null)
            return type;
        if (javaType instanceof Class<?> k && (Value.class == k || Reference.class == k))
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
            return getDef(javaType, INIT).getType();
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
    public void setParameterizedMap(ParameterizedMap map) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ParameterizedMap getParameterizedMap() {
        throw new UnsupportedOperationException();
    }


    @Override
    public <T extends Entity> List<T> query(EntityIndexQuery<T> query) {
        return List.of();
    }

    @Override
    public long count(EntityIndexQuery<?> query) {
        return 0;
    }

}
