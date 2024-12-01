package org.metavm.entity;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.metavm.object.instance.core.IInstanceContext;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.Value;
import org.metavm.object.type.*;
import org.metavm.util.*;

import javax.annotation.Nullable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;

import static org.metavm.object.type.ResolutionStage.*;

@Slf4j
public abstract class DefContext extends BaseEntityContext implements IEntityContext, TypeRegistry {

    public static final Map<Class<?>, Class<?>> BOX_CLASS_MAP = Map.ofEntries(
            Map.entry(Byte.class, Long.class),
            Map.entry(Short.class, Long.class),
            Map.entry(Integer.class, Long.class),
            Map.entry(Float.class, Double.class)
    );

    public DefContext(IInstanceContext instanceContext) {
        super(instanceContext, null);
    }

    @SuppressWarnings("unchecked")
    public <T extends Enum<?>> EnumDef<T> getEnumDef(Class<T> enumType) {
        return (EnumDef<T>) getDef(enumType);
    }

    public Mapper<?, ?> getMapper(org.metavm.object.type.Type type) {
        return Objects.requireNonNull(tryGetMapper(type));
    }

    public @Nullable Mapper<?, ?> tryGetMapper(int typeTag) {
        if (!TypeTags.isSystemTypeTag(typeTag))
            throw new IllegalArgumentException("Can not get mapper for type tag: " + typeTag);
        if (typeTag <= TypeTags.VALUE_ARRAY) {
            var javaClass = switch (typeTag) {
                case TypeTags.READONLY_ARRAY -> ReadonlyArray.class;
                case TypeTags.READ_WRITE_ARRAY -> ReadWriteArray.class;
                case TypeTags.CHILD_ARRAY -> ChildArray.class;
                case TypeTags.VALUE_ARRAY ->  ValueArray.class;
                default -> throw new IllegalStateException("Should not reach here");
            };
            //noinspection rawtypes,unchecked
            return new ArrayMapper<>(javaClass, this);
        } else
            return tryGetDef(typeTag);
    }

    protected abstract @Nullable ModelDef<?> tryGetDef(int typeTag);

    public Mapper<?, ?> getMapper(int typeTag) {
        return Objects.requireNonNull(tryGetMapper(typeTag), () -> "Cannot find mapper for type tag: " + typeTag);
    }

    public Mapper<?, ?> getMapper(Type javaType) {
        return getMapper(javaType, ResolutionStage.INIT);
    }

    public <T> PojoDef<T> getPojoDef(Class<T> klass) {
        return new TypeReference<PojoDef<T>>() {}.cast(
                getDef(klass)
        );
    }

    @Nullable
    public Mapper<?, ?> tryGetMapper(org.metavm.object.type.Type type) {
        if (type instanceof ArrayType arrayType) {
            var javaClass = switch (arrayType.getKind()) {
                case CHILD -> ChildArray.class;
                case READ_WRITE -> ReadWriteArray.class;
                case READ_ONLY -> ReadonlyArray.class;
                case VALUE -> ValueArray.class;
            };
            //noinspection rawtypes,unchecked
            return new ArrayMapper<>(javaClass, this);
        } else if (type instanceof ClassType classType)
            return tryGetDef(classType.getKlass());
        else
            throw new InternalException("Can not get entity mapper for type: " + type.getTypeDesc());
    }

    public abstract ModelDef<?> tryGetDef(TypeDef typeDef);

    public ModelDef<?> getDef(TypeDef typeDef) {
        return Objects.requireNonNull(tryGetDef(typeDef), "Can not find def for: " + typeDef);
    }

    public abstract ModelDef<?> getDef(Type javaType, ResolutionStage stage);

    public abstract @Nullable ModelDef<?> getDefIfPresent(Type javaType);

    public Type getJavaType(org.metavm.object.type.Type type) {
        var javaType = Types.getPrimitiveJavaType(type);
        if (javaType != null)
            return javaType;
        if (type.isBinaryNullable())
            return BiUnion.createNullableType(getJavaType(type.getUnderlyingType())); // TODO maybe we should not use BiUnion here
        if (type instanceof ArrayType arrayType)
            return ParameterizedTypeImpl.create(arrayType.getKind().getEntityClass(), getJavaType(arrayType.getElementType()));
        else if (type instanceof ClassType classType)
            return getDef(classType.getKlass()).getEntityType();
        else
            throw new IllegalArgumentException("Can not get java type for type " + type);
    }

    public java.lang.reflect.Field getJavaField(Field field) {
        return Objects.requireNonNull(findJavaField(field), () -> "Cannot find Java field for field " + field);
    }

    public @Nullable java.lang.reflect.Field findJavaField(Field field) {
        var def = (PojoDef<?>) getDef(field.getDeclaringType());
        var fieldDef =  def.findFieldDef(fd -> fd.getField() == field);
        return fieldDef != null ? fieldDef.getJavaField() : null;
    }

    public org.metavm.object.type.Field getField(java.lang.reflect.Field javaField) {
        return ((PojoDef<?>) getDef(javaField.getDeclaringClass(), DECLARATION)).getKlass().getFieldByJavaField(javaField);
    }

    public abstract Collection<ModelDef<?>> getAllDefList();

    public Class<?> getJavaClass(org.metavm.object.type.Type type) {
        return getMapper(type).getEntityClass();
    }

    public Index getIndexConstraint(IndexDef<?> indexDef) {
        EntityDef<?> entityDef = (EntityDef<?>) getDef(indexDef.getType());
        return entityDef.getIndexConstraintDef(indexDef).getIndexConstraint();
    }

    public  <T> ModelDef<T> getDef(Class<T> klass) {
        //noinspection unchecked
        return (ModelDef<T>) getDef((Type) klass);
    }

    public ModelDef<?> getDef(Type javaType) {
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
        if (javaType instanceof Class<?> k && Value.class.isAssignableFrom(k))
            return AnyType.instance;
        if (BiUnion.isNullable(javaType))
            return Types.getNullableType(getType(BiUnion.getUnderlyingType(javaType)));
        if (javaType instanceof ParameterizedType pType) {
            var rawClass = (Class<?>) pType.getRawType();
            var typeArgs = pType.getActualTypeArguments();
            if (ReadonlyArray.class.isAssignableFrom(rawClass))
                return new ArrayType(getType(typeArgs[0]), ArrayKind.fromEntityClass(rawClass));
            else
                return new ClassType(
                        pType.getOwnerType() != null ? (ClassType) getDef(pType.getOwnerType()).getType() : null,
                        ((ClassType) getDef(rawClass).getType()).getKlass(), NncUtils.map(pType.getActualTypeArguments(), this::getType)
                );
        } else
            return getDef(javaType, INIT).getType();
    }

    public Mapper<?, ?> getMapperByEntity(Object entity) {
        if(entity instanceof RuntimeGeneric runtimeGeneric) {
            if (runtimeGeneric.getGenericType() == null) {
                runtimeGeneric.getGenericType();
                throw new RuntimeException("Encountering a runtime generic with null generic type: " + runtimeGeneric);
            }
            return getMapper(runtimeGeneric.getGenericType(), ResolutionStage.DEFINITION);
        }
        else
            return getMapper(entity.getClass(), ResolutionStage.DEFINITION);
    }

    @NotNull
    private static ArrayKind getArrayKind(Class<?> rawClass) {
        if (rawClass == ReadWriteArray.class)
            return ArrayKind.READ_WRITE;
        if (rawClass == ChildArray.class)
            return ArrayKind.CHILD;
        if (rawClass == ValueArray.class)
            return ArrayKind.VALUE;
        if (rawClass == ReadonlyArray.class)
            return ArrayKind.READ_ONLY;
        throw new InternalException("Unrecognized array class " + rawClass.getName());
    }

    private Class<?> getArrayClass(ArrayKind arrayKind) {
        return switch (arrayKind) {
            case CHILD -> ChildArray.class;
            case READ_WRITE -> ReadWriteArray.class;
            case READ_ONLY -> ReadonlyArray.class;
            case VALUE -> ValueArray.class;
        };
    }

    private @Nullable ArrayType tryParseArrayType(Type javaType) {
        if (javaType instanceof ParameterizedType pType) {
            var rawClass = (Class<?>) pType.getRawType();
            if (ReadonlyArray.class.isAssignableFrom(rawClass)) {
                var elementType = getType(pType.getActualTypeArguments()[0]);
                ArrayKind arrayKind = getArrayKind(rawClass);
                return new ArrayType(elementType, arrayKind);
            }
        }
        return null;
    }

    public Mapper<?, ?> getMapper(Type javaType, ResolutionStage stage) {
        var arrayType = tryParseArrayType(javaType);
        if (arrayType != null) {
            var pType = (ParameterizedType) javaType;
            var arrayClass = getArrayClass(arrayType.getKind());
            var elementType = pType.getActualTypeArguments()[0];
            if (elementType instanceof Class<?> klass && Value.class.isAssignableFrom(klass))
                //noinspection rawtypes,unchecked
                return new InstanceArrayMapper(arrayClass, pType, Value.class, arrayType);
            else
                //noinspection rawtypes,unchecked
                return new ArrayMapper(arrayClass, this);
        } else if (javaType instanceof Class<?> klass && Instance.class.isAssignableFrom(klass))
            //noinspection rawtypes,unchecked
            return new InstanceMapper(Instance.class.asSubclass(klass));
        else
            return getDef(javaType, stage);
    }

    public abstract boolean containsDef(TypeDef typeDef);

    protected void checkJavaType(Type javaType) {
        if (javaType instanceof WildcardType && javaType instanceof java.lang.reflect.TypeVariable<?>) {
            throw new InternalException("Can not get def for java type '" + javaType.getTypeName() + "', " +
                    "Because it's either a wildcard type or a type variable");
        }
    }

    public Klass getKlass(Class<?> javaClass) {
        return (Klass) getDef(javaClass).getTypeDef();
    }

    public Klass tryGetKlass(Class<?> javaClass) {
        var def = getDefIfPresent(javaClass);
        return def != null ? (Klass) def.getTypeDef() : null;
    }

    public org.metavm.object.type.Type getType(Class<?> javaClass) {
        return getType((Type) javaClass);
    }

    public ClassType getClassType(Class<?> javaType) {
        return (ClassType) getType(javaType);
    }

    @SuppressWarnings("unused")
    public Field getField(Class<?> javaType, String javaFieldName) {
        return getField(ReflectionUtils.getField(javaType, javaFieldName));
    }

    public <T extends Entity> EntityDef<T> getEntityDef(TypeReference<T> typeReference) {
        return getEntityDef(typeReference.getType());
    }

    public <T extends Entity> EntityDef<T> getEntityDef(Class<T> klass) {
        return new TypeReference<EntityDef<T>>() {}.cast(
                getDef(klass)
        );
    }

    public abstract Class<?> getJavaClassByTag(int tag);
}
