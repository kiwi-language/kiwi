package tech.metavm.entity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.metavm.object.type.*;
import tech.metavm.util.Never;
import tech.metavm.util.Null;
import tech.metavm.util.Password;

import javax.annotation.Nullable;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

public class StandardTypes {

    public static final Logger logger = LoggerFactory.getLogger(StandardTypes.class);

    private static StandardTypesHolder holder = new GlobalStandardTypesHolder();

    public static void setHolder(StandardTypesHolder holder) {
        StandardTypes.holder = holder;
    }

    private static final Function<java.lang.reflect.Type, Type> getType = ModelDefRegistry::getType;

    public static Type getType(java.lang.reflect.Type javaType) {
        return getType.apply(javaType);
    }

    public static @Nullable Class<?> getPrimitiveJavaType(Type type) {
        if (type instanceof PrimitiveType primitiveType) {
            return switch (primitiveType.getKind()) {
                case BOOLEAN -> Boolean.class;
                case LONG -> Long.class;
                case STRING -> String.class;
                case TIME -> Date.class;
                case NULL -> Null.class;
                case VOID -> Void.class;
                case PASSWORD -> Password.class;
                case DOUBLE -> Double.class;
            };
        }
        if(type instanceof AnyType)
            return Object.class;
        if(type instanceof NeverType)
            return Never.class;
        return null;
    }

    public static @Nullable Type getPrimitiveType(java.lang.reflect.Type javaType) {
        if (javaType instanceof Class<?> javaClass) {
            if (javaClass == Object.class)
                return getAnyType();
            if (javaClass == Never.class)
                return getNeverType();
            if (javaClass == String.class)
                return getStringType();
            if (javaClass == Long.class || javaClass == long.class
                    || javaClass == Integer.class || javaClass == int.class
                    || javaClass == Short.class || javaClass == short.class
                    || javaClass == Byte.class || javaClass == byte.class)
                return getLongType();
            if (javaClass == Boolean.class || javaClass == boolean.class)
                return getBooleanType();
            if (javaClass == Double.class || javaClass == double.class)
                return getDoubleType();
            if (javaClass == Void.class || javaClass == void.class)
                return getVoidType();
            if (javaClass == Password.class)
                return getPasswordType();
            if (javaClass == Date.class)
                return getTimeType();
            if (javaClass == Null.class)
                return getNullType();
        }
        return null;
    }

    public static ClassType getClassType(java.lang.reflect.Type javaType) {
        return (ClassType) getType.apply(javaType);
    }

    public static AnyType getAnyType() {
        return new AnyType();
    }

    public static UnionType getNullableAnyType() {
        return new UnionType(Set.of(new AnyType(), new PrimitiveType(PrimitiveKind.NULL)));
    }

    public static Type getAnyType(boolean nullable) {
        return nullable ? getNullableAnyType() : getAnyType();
    }

    public static Klass getEnumKlass() {
        return holder.getEnumKlass();
    }

    public static Klass getEntityKlass() {
        return holder.getEntityKlass();
    }

    public static Klass getRecordKlass() {
        return holder.getRecordKlass();
    }

    public static PrimitiveType getBooleanType() {
        return new PrimitiveType(PrimitiveKind.BOOLEAN);
    }

    public static PrimitiveType getLongType() {
        return new PrimitiveType(PrimitiveKind.LONG);
    }

    public static PrimitiveType getStringType() {
        return new PrimitiveType(PrimitiveKind.STRING);
    }

    public static PrimitiveType getTimeType() {
        return new PrimitiveType(PrimitiveKind.TIME);
    }

    public static PrimitiveType getNullType() {
        return new PrimitiveType(PrimitiveKind.NULL);
    }

    public static PrimitiveType getVoidType() {
        return new PrimitiveType(PrimitiveKind.VOID);
    }

    public static PrimitiveType getPasswordType() {
        return new PrimitiveType(PrimitiveKind.PASSWORD);
    }

    public static PrimitiveType getDoubleType() {
        return new PrimitiveType(PrimitiveKind.DOUBLE);
    }

    public static ArrayType getAnyArrayType() {
        return new ArrayType(new AnyType(), ArrayKind.READ_WRITE);
    }

    public static ArrayType getNeverArrayType() {
        return new ArrayType(new NeverType(), ArrayKind.READ_WRITE);
    }

    public static ArrayType getReadOnlyAnyArrayType() {
        return new ArrayType(new AnyType(), ArrayKind.READ_ONLY);
    }

    public static ArrayType getChildAnyArrayType() {
        return new ArrayType(new AnyType(), ArrayKind.CHILD);
    }

    public static Field getEnumNameField(Klass classType) {
        return classType.getFieldByCode("name");
    }

    public static Field getEnumOrdinalField(Klass classType) {
        return classType.getFieldByCode("ordinal");
    }

    public static Klass getListKlass() {
        return holder.getListKlass();
    }

    public static Klass getReadWriteListKlass() {
        return holder.getReadWriteListKlass();
    }

    public static Klass getReadWriteListKlass(Type elementType) {
        return new ClassType(getReadWriteListKlass(), List.of(elementType)).resolve();
    }

    public static Klass getChildListKlass() {
        return Objects.requireNonNull(holder.getChildListKlass());
    }

    public static Klass getValueListKlass() {
        return Objects.requireNonNull(holder.getValueListKlass());
    }

    public static Klass getSetKlass() {
        return Objects.requireNonNull(holder.getSetKlass());
    }

    public static Klass getMapKlass() {
        return Objects.requireNonNull(holder.getMapKlass());
    }

    public static Klass getCollectionKlass() {
        return Objects.requireNonNull(holder.getCollectionKlass());
    }

    public static Klass getIterableKlass() {
        return Objects.requireNonNull(holder.getIterableKlass());
    }

    public static Klass getIteratorKlass() {
        return Objects.requireNonNull(holder.getIteratorKlass());
    }

    public static Klass getIteratorImplKlass() {
        return Objects.requireNonNull(holder.getIteratorImplKlass());
    }

    public static Klass getThrowableKlass() {
        return Objects.requireNonNull(holder.getThrowableKlass());
    }

    public static Klass getExceptionKlass() {
        return Objects.requireNonNull(holder.getExceptionKlass());
    }

    public static Klass getRuntimeExceptionKlass() {
        return Objects.requireNonNull(holder.getRuntimeExceptionKlass());
    }

    public static UnionType getNullableThrowableKlass() {
        return UnionType.create(getThrowableKlass().getType(), getNullType());
    }

    public static UnionType getNullableStringType() {
        return new UnionType(Set.of(getStringType(), getNullType()));
    }

    public static Klass getConsumerKlass() {
        return Objects.requireNonNull(holder.getConsumerKlass());
    }

    public static void setConsumerKlass(Klass type) {
        holder.setConsumerKlass(type);
    }

    public static Klass getPredicateKlass() {
        return Objects.requireNonNull(holder.getPredicateKlass());
    }

    public static void setPredicateKlass(Klass type) {
        holder.setPredicateKlass(type);
    }

    public static NeverType getNeverType() {
        return new NeverType();
    }

    public static void setEnumKlass(Klass enumKlass) {
        holder.setEnumKlass(enumKlass);
    }

    public static void setThrowableKlass(Klass throwableKlas) {
        holder.setThrowableKlass(throwableKlas);
    }

    public static void setExceptionKlass(Klass exceptionKlass) {
        holder.setExceptionKlass(exceptionKlass);
    }

    public static void setRuntimeExceptionKlass(Klass runtimeExceptionKlass) {
        holder.setRuntimeExceptionKlass(runtimeExceptionKlass);
    }

    public static void setIllegalArgumentExceptionKlass(Klass illegalArgumentExceptionKlass) {
        holder.setIllegalArgumentExceptionKlass(illegalArgumentExceptionKlass);
    }

    public static void setIllegalStateExceptionKlass(Klass illegalStateExceptionKlass) {
        holder.setIllegalStateExceptionKlass(illegalStateExceptionKlass);
    }

    public static Klass getNullPointerExceptionKlass() {
        return holder.getNullPointerExceptionKlass();
    }

    public static void setNullPointerExceptionKlass(Klass nullPointerExceptionKlass) {
        holder.setNullPointerExceptionKlass(nullPointerExceptionKlass);
    }

    public static Klass getIllegalArgumentExceptionKlass() {
        return holder.getIllegalArgumentExceptionKlass();
    }

    public static Klass getIllegalStateExceptionKlass() {
        return holder.getIllegalStateExceptionKlass();
    }

    public static void setEntityKlass(Klass entityKlass) {
        holder.setEntityKlass(entityKlass);
    }

    public static Klass setRecordKlass(Klass recordKlass) {
        holder.setRecordKlass(recordKlass);
        return recordKlass;
    }

    public static void setCollectionKlass(Klass collectionKlass) {
        holder.setCollectionKlass(collectionKlass);
    }

    public static void setSetKlass(Klass setKlass) {
        holder.setSetKlass(setKlass);
    }

    public static void setListKlass(Klass listKlass) {
        holder.setListKlass(listKlass);
    }

    public static void setReadWriteListKlass(Klass readWriteListKlass) {
        holder.setReadWriteListKlass(readWriteListKlass);
    }

    public static void setChildListKlass(Klass childListKlass) {
        holder.setChildListKlass(childListKlass);
    }

    public static void setValueListKlass(Klass childListKlass) {
        holder.setValueListKlass(childListKlass);
    }

    public static void setMapKlass(Klass mapKlass) {
        holder.setMapKlass(mapKlass);
    }

    public static void setIteratorKlass(Klass iteratorKlass) {
        holder.setIteratorKlass(iteratorKlass);
    }

    public static void setIterableKlass(Klass iterableKlass) {
        holder.setIterableKlass(iterableKlass);
    }

    public static void setIteratorImplKlass(Klass iteratorImplKlass) {
        holder.setIteratorImplKlass(iteratorImplKlass);
    }

    public static UnionType getNullableType(Type type) {
        return new UnionType(Set.of(type, getNullType()));
    }

}