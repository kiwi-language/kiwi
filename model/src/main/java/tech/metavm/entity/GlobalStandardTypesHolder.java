package tech.metavm.entity;

import tech.metavm.object.type.*;
import tech.metavm.util.InternalException;
import tech.metavm.util.NncUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class GlobalStandardTypesHolder implements StandardTypesHolder {

    private PrimitiveType longType;
    private PrimitiveType doubleType;
    private PrimitiveType booleanType;
    private PrimitiveType stringType;
    private PrimitiveType passwordType;
    private PrimitiveType timeType;
    private PrimitiveType nullType;
    private PrimitiveType voidType;
    private NeverType neverType;
    private AnyType anyType;
    private UnionType nullableAnyType;
    private ArrayType anyArrayType;
    private ArrayType neverArrayType;
    private UnionType nullableStringType;
    private Klass enumType;
    private Klass throwableType;
    private Klass exceptionType;
    private Klass runtimeExceptionType;
    private Klass entityType;
    private Klass recordType;
    private Klass collectionType;
    private Klass setType;
    private Klass listType;
    private Klass mapType;
    private Klass iteratorType;
    private Klass iteratorImplType;
    private ArrayType readonlyAnyArrayType;
    private Klass childListType;
    private Klass readWriteListType;
    private Klass iterableType;
    private Klass consumerType;
    private Klass predicateType;
    private Klass illegalArgumentExceptionType;
    private Klass illegalStateExceptionType;
    private Klass nullPointerExceptionType;
    private final Map<PTypeKey, Klass> parameterizedTypes = new HashMap<>();
    private final Map<Type, UnionType> nullableTypes = new HashMap<>();

    @Override
    public PrimitiveType getLongType() {
        return longType;
    }

    @Override
    public PrimitiveType getDoubleType() {
        return doubleType;
    }

    @Override
    public PrimitiveType getBooleanType() {
        return booleanType;
    }

    @Override
    public PrimitiveType getStringType() {
        return stringType;
    }

    @Override
    public PrimitiveType getTimeType() {
        return timeType;
    }

    @Override
    public PrimitiveType getVoidType() {
        return voidType;
    }

    @Override
    public PrimitiveType getNullType() {
        return nullType;
    }

    @Override
    public NeverType getNeverType() {
        return neverType;
    }

    @Override
    public AnyType getAnyType() {
        return anyType;
    }

    @Override
    public ArrayType getAnyArrayType() {
        return anyArrayType;
    }

    @Override
    public ArrayType getNeverArrayType() {
        return neverArrayType;
    }

    @Override
    public UnionType getNullableAnyType() {
        return nullableAnyType;
    }

    @Override
    public UnionType getNullableStringType() {
        return nullableStringType;
    }

    @Override
    public Klass getEnumType() {
        return enumType;
    }

    @Override
    public Klass getThrowableType() {
        return throwableType;
    }

    @Override
    public Klass getExceptionType() {
        return exceptionType;
    }

    @Override
    public Klass getRuntimeExceptionType() {
        return runtimeExceptionType;
    }

    @Override
    public Klass getEntityType() {
        return entityType;
    }

    @Override
    public Klass getRecordType() {
        return recordType;
    }

    @Override
    public Klass getCollectionType() {
        return collectionType;
    }

    @Override
    public Klass getSetType() {
        return setType;
    }

    @Override
    public Klass getListType() {
        return listType;
    }

    @Override
    public Klass getMapType() {
        return mapType;
    }

    @Override
    public Klass getIteratorType() {
        return iteratorType;
    }

    @Override
    public Klass getIteratorImplType() {
        return iteratorImplType;
    }

    @Override
    public ArrayType getReadonlyAnyArrayType() {
        return readonlyAnyArrayType;
    }

    @Override
    public PrimitiveType getPasswordType() {
        return passwordType;
    }

    @Override
    public void setLongType(PrimitiveType type) {
        longType = type;
    }

    @Override
    public void setDoubleType(PrimitiveType type) {
        doubleType = type;
    }

    @Override
    public void setBooleanType(PrimitiveType type) {
        booleanType = type;
    }

    @Override
    public void setStringType(PrimitiveType type) {
        stringType = type;
    }

    @Override
    public void setTimeType(PrimitiveType type) {
        timeType = type;
    }

    @Override
    public void setVoidType(PrimitiveType type) {
        voidType = type;
    }

    @Override
    public void setNullType(PrimitiveType type) {
        nullType = type;
    }

    @Override
    public void setPasswordType(PrimitiveType type) {
        passwordType = type;
    }

    @Override
    public void setNeverType(NeverType type) {
        neverType = type;
    }

    @Override
    public void setAnyType(AnyType type) {
        anyType = type;
    }

    @Override
    public void setAnyArrayType(ArrayType type) {
        anyArrayType = type;
    }

    @Override
    public void setNeverArrayType(ArrayType type) {
        neverArrayType = type;
    }

    @Override
    public void setNullableAnyType(UnionType type) {
        nullableAnyType = type;
    }

    @Override
    public void setNullableStringType(UnionType type) {
        nullableStringType = type;
    }

    @Override
    public void setEnumType(Klass type) {
        enumType = type;
    }

    @Override
    public void setThrowableType(Klass type) {
        throwableType = type;
    }

    @Override
    public void setExceptionType(Klass type) {
        exceptionType = type;
    }

    @Override
    public void setRuntimeExceptionType(Klass type) {
        runtimeExceptionType = type;
    }

    @Override
    public void setEntityType(Klass type) {
        entityType = type;
    }

    @Override
    public void setRecordType(Klass type) {
        recordType = type;
    }

    @Override
    public void setCollectionType(Klass type) {
        collectionType = type;
    }

    @Override
    public void setSetType(Klass type) {
        setType = type;
    }

    @Override
    public void setListType(Klass type) {
        listType = type;
    }

    @Override
    public void setMapType(Klass type) {
        mapType = type;
    }

    @Override
    public void setIteratorType(Klass type) {
        iteratorType = type;
    }

    @Override
    public void setIteratorImplType(Klass type) {
        iteratorImplType = type;
    }

    @Override
    public void setReadonlyAnyArrayType(ArrayType type) {
        readonlyAnyArrayType = type;
    }

    @Override
    public void setChildListType(Klass type) {
        childListType = type;
    }

    @Override
    public void setReadWriteListType(Klass type) {
        readWriteListType = type;
    }

    @Override
    public Klass getChildListType() {
        return childListType;
    }

    @Override
    public Klass getReadWriteListType() {
        return readWriteListType;
    }

    @Override
    public Klass getIterableType() {
        return iterableType;
    }

    @Override
    public void setIterableType(Klass iterableType) {
        this.iterableType = iterableType;
    }

    @Override
    public Klass getConsumerType() {
        return consumerType;
    }

    @Override
    public void setConsumerType(Klass consumerType) {
        this.consumerType = consumerType;
    }

    @Override
    public Klass getPredicateType() {
        return predicateType;
    }

    @Override
    public void setPredicateType(Klass type) {
        predicateType = type;
    }

    @Override
    public void setIllegalArgumentExceptionType(Klass illegalArgumentExceptionType) {
        this.illegalArgumentExceptionType = illegalArgumentExceptionType;
    }

    @Override
    public void setIllegalStateExceptionType(Klass illegalStateExceptionType) {
        this.illegalStateExceptionType = illegalStateExceptionType;
    }

    @Override
    public Klass getIllegalArgumentExceptionType() {
        return illegalArgumentExceptionType;
    }

    @Override
    public Klass getIllegalStateExceptionType() {
        return illegalStateExceptionType;
    }

    @Override
    public Klass getNullPointerExceptionType() {
        return nullPointerExceptionType;
    }

    @Override
    public void setNullPointerExceptionType(Klass nullPointerExceptionType) {
        this.nullPointerExceptionType = nullPointerExceptionType;
    }

    @Override
    public Klass getParameterizedType(Klass template, List<Type> typeArguments) {
        return Objects.requireNonNull(parameterizedTypes.get(new PTypeKey(template, typeArguments)),
                () -> "Parameterized type not found: " + template.getName() + "<" + NncUtils.join(typeArguments, Type::getName) + "> in standard types");
}

    @Override
    public void addParameterizedType(Klass type) {
        parameterizedTypes.put(new PTypeKey(Objects.requireNonNull(type.getTemplate()), type.getTypeArguments()), type);
    }

    @Override
    public void clearParameterizedTypes() {
        parameterizedTypes.clear();
    }

    @Override
    public void addNullableType(UnionType type) {
        if(type.isBinaryNullable())
            nullableTypes.put(type.getUnderlyingType(), type);
        else
            throw new InternalException(type.getTypeDesc() + " is not a nullable type");
    }

    @Override
    public UnionType getNullableType(Type type) {
        return Objects.requireNonNull(nullableTypes.get(type), () -> "Can not find nullable type for " + type.getTypeDesc() + " in standard types");
    }

    @Override
    public void clearNullableTypes() {
        this.nullableTypes.clear();
    }

}
