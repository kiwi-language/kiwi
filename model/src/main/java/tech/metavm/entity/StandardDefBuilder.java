package tech.metavm.entity;

import tech.metavm.entity.natives.*;
import tech.metavm.flow.FunctionBuilder;
import tech.metavm.flow.Method;
import tech.metavm.flow.MethodBuilder;
import tech.metavm.flow.Parameter;
import tech.metavm.object.instance.ColumnKind;
import tech.metavm.object.instance.core.BooleanInstance;
import tech.metavm.object.instance.core.NullInstance;
import tech.metavm.object.type.*;
import tech.metavm.util.*;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static tech.metavm.object.type.Types.getParameterizedCode;
import static tech.metavm.object.type.Types.getParameterizedName;
import static tech.metavm.util.ReflectionUtils.ENUM_NAME_FIELD;
import static tech.metavm.util.ReflectionUtils.ENUM_ORDINAL_FIELD;

public class StandardDefBuilder {

    private DirectDef<Object> objectDef;

    private ValueDef<Enum<?>> enumDef;

    private FieldDef enumNameDef;

    private FieldDef enumOrdinalDef;

    private Map<Class<?>, PrimitiveType> primitiveTypeMap;

    private final DefContext defContext;

    private final PrimTypeFactory primTypeFactory = new PrimTypeFactory();

    private static final Map<java.lang.reflect.Type, Class<?>> NATIVE_CLASS_MAP = Map.ofEntries(
            Map.entry(MetaSet.class, SetNative.class),
            Map.entry(ChildMetaList.class, ListNative.class),
            Map.entry(ReadWriteMetaList.class, ListNative.class),
            Map.entry(MetaList.class, ListNative.class),
            Map.entry(MetaMap.class, MapNative.class),
            Map.entry(IteratorImpl.class, IteratorImplNative.class),
            Map.entry(Throwable.class, ThrowableNative.class),
            Map.entry(Exception.class, ExceptionNative.class),
            Map.entry(RuntimeException.class, RuntimeExceptionNative.class),
            Map.entry(IllegalArgumentException.class, IllegalArgumentExceptionNative.class),
            Map.entry(IllegalStateException.class, IllegalStateExceptionNative.class),
            Map.entry(NullPointerException.class, NullPointerExceptionNative.class)
    );

    public StandardDefBuilder(DefContext defContext) {
        this.defContext = defContext;
    }

    public void initRootTypes() {
        StandardTypes.setAnyType(new AnyType());
        objectDef = new DirectDef<>(Object.class, StandardTypes.getAnyType());
        StandardTypes.setNeverType(new NeverType());
        var neverDef = new DirectDef<>(Never.class, StandardTypes.getNeverType());
        defContext.addDef(neverDef);

        primTypeFactory.addAuxType(Never.class, StandardTypes.getNeverType());
        primTypeFactory.addAuxType(Object.class, StandardTypes.getAnyType());
        StandardTypes.setNullType(primTypeFactory.createPrimitive(PrimitiveKind.NULL));
        StandardTypes.setLongType(primTypeFactory.createPrimitive(PrimitiveKind.LONG));
        StandardTypes.setDoubleType(primTypeFactory.createPrimitive(PrimitiveKind.DOUBLE));
        StandardTypes.setStringType(primTypeFactory.createPrimitive(PrimitiveKind.STRING));
        StandardTypes.setBooleanType(primTypeFactory.createPrimitive(PrimitiveKind.BOOLEAN));
        StandardTypes.setTimeType(primTypeFactory.createPrimitive(PrimitiveKind.TIME));
        StandardTypes.setPasswordType(primTypeFactory.createPrimitive(PrimitiveKind.PASSWORD));
        StandardTypes.setVoidType(primTypeFactory.createPrimitive(PrimitiveKind.VOID));
        Instances.setNullInstance(new NullInstance(StandardTypes.getNullType()));
        Instances.setTrueInstance(new BooleanInstance(true, StandardTypes.getBooleanType()));
        Instances.setFalseInstance(new BooleanInstance(false, StandardTypes.getBooleanType()));
        primitiveTypeMap = Map.ofEntries(
                Map.entry(Null.class, StandardTypes.getNullType()),
                Map.entry(Long.class, StandardTypes.getLongType()),
                Map.entry(Double.class, StandardTypes.getDoubleType()),
                Map.entry(String.class, StandardTypes.getStringType()),
                Map.entry(Boolean.class, StandardTypes.getBooleanType()),
                Map.entry(Date.class, StandardTypes.getTimeType()),
                Map.entry(Password.class, StandardTypes.getPasswordType()),
                Map.entry(Void.class, StandardTypes.getVoidType())
        );

        primitiveTypeMap.forEach((klass, primType) -> defContext.addDef(new DirectDef<>(klass, primType)));

        defContext.addDef(objectDef);
        defContext.createCompositeTypes(StandardTypes.getAnyType());
        StandardTypes.setNullableAnyType(defContext.getNullableType(StandardTypes.getAnyType()));
        StandardTypes.setAnyArrayType(defContext.getArrayType(StandardTypes.getAnyType(), ArrayKind.READ_WRITE));
        StandardTypes.setReadonlyAnyArrayType(defContext.getArrayType(StandardTypes.getAnyType(), ArrayKind.READ_ONLY));
        StandardTypes.setNeverArrayType(defContext.getArrayType(StandardTypes.getNeverType(), ArrayKind.READ_WRITE));

        initBuiltinFlows();

        var collectionTypeMap = new LinkedHashMap<Class<?>, Klass>();
        collectionTypeMap.put(Consumer.class, StandardTypes.setConsumerType(createConsumerType()));
        collectionTypeMap.put(Predicate.class, StandardTypes.setPredicateType(createPredicateType()));
        collectionTypeMap.put(MetaIterator.class, StandardTypes.setIteratorType(createIteratorType()));
        collectionTypeMap.put(MetaIterable.class, StandardTypes.setIterableType(createIterableType()));
        collectionTypeMap.put(Collection.class, StandardTypes.setCollectionType(createCollectionType()));
        collectionTypeMap.put(IteratorImpl.class, StandardTypes.setIteratorImplType(createIteratorImplType()));
        collectionTypeMap.put(MetaSet.class, StandardTypes.setSetType(createSetType()));
        collectionTypeMap.put(MetaList.class, StandardTypes.setListType(createListType()));
        collectionTypeMap.put(ReadWriteMetaList.class, StandardTypes.setReadWriteListType(createReadWriteListType()));
        collectionTypeMap.put(ChildMetaList.class, StandardTypes.setChildListType(createChildListType()));
        collectionTypeMap.put(MetaMap.class, StandardTypes.setMapType(createMapType()));

        for (var entry : primitiveTypeMap.entrySet()) {
            var primClass = entry.getKey();
            var primType = entry.getValue();
            if (!primType.isNull() && !primType.isVoid()) {
                defContext.createCompositeTypes(primType);
                collectionTypeMap.forEach((collClass, collType) -> {
                    if (collClass != MetaMap.class) {
                        StandardTypes.addParameterizedType(defContext.getGenericContext().getParameterizedType(collType, primType));
                        StandardTypes.addNullableType(defContext.getNullableType(primType));
                    }
                });
            }
        }
        StandardTypes.setNullableStringType(defContext.getNullableType(StandardTypes.getStringType()));
        ValueDef<Record> recordDef = createValueDef(
                Record.class,
                Record.class,
                StandardTypes.setRecordType(ClassTypeBuilder.newBuilder("记录", Record.class.getSimpleName())
                        .source(ClassSource.BUILTIN)
                        .kind(ClassKind.VALUE).build()),
                defContext
        );
        defContext.addDef(recordDef);

        StandardTypes.setEntityType(ClassTypeBuilder.newBuilder("实体", Entity.class.getSimpleName())
                .source(ClassSource.BUILTIN)
                .build());
        EntityDef<Entity> entityDef = createEntityDef(
                Entity.class,
                Entity.class,
                StandardTypes.getEntityType(),
                defContext
        );

        defContext.addDef(entityDef);

        var enumTypeParam = new TypeVariable(null, "枚举类型", "EnumType",
                DummyGenericDeclaration.INSTANCE);
        primTypeFactory.addAuxType(Enum.class.getTypeParameters()[0], enumTypeParam.getType());
        StandardTypes.setEnumType(ClassTypeBuilder.newBuilder("枚举", Enum.class.getSimpleName())
                .source(ClassSource.BUILTIN)
                .typeParameters(enumTypeParam)
                .build());
        primTypeFactory.addAuxType(Enum.class, StandardTypes.getEnumType().getType());

//        var pEnumType = defContext.getGenericContext().getParameterizedType(enumType, enumTypeParam);
        enumTypeParam.setBounds(List.of(StandardTypes.getEnumType().getType()));

        enumDef = createValueDef(
                Enum.class,// Enum is not a RuntimeGeneric, use the raw class
                new TypeReference<Enum<?>>() {
                }.getType(),
                StandardTypes.getEnumType(),
                defContext
        );

        enumNameDef = createFieldDef(
                ENUM_NAME_FIELD,
                createField(ENUM_NAME_FIELD, true, StandardTypes.getStringType(), Access.PUBLIC,
                        ColumnKind.STRING.getColumn(0), StandardTypes.getEnumType()),
                enumDef
        );

        enumOrdinalDef = createFieldDef(
                ENUM_ORDINAL_FIELD,
                createField(ENUM_ORDINAL_FIELD, false, StandardTypes.getLongType(), Access.PRIVATE,
                        ColumnKind.INT.getColumn(0), StandardTypes.getEnumType()),
                enumDef
        );
        StandardTypes.getEnumType().setTitleField(enumNameDef.getField());
        StandardTypes.getEnumType().setStage(ResolutionStage.DEFINITION);
        defContext.getGenericContext().generateCode(StandardTypes.getEnumType());

        var enumTypeParamDef = new TypeVariableDef(Enum.class.getTypeParameters()[0], enumTypeParam);
//        var pEnumTypeDef = new DirectDef<>(
//                ParameterizedTypeImpl.create(Enum.class, Enum.class.getTypeParameters()[0]),
//                pEnumType
//        );
        defContext.preAddDef(enumTypeParamDef);
//        defContext.preAddDef(pEnumTypeDef);
        defContext.addDef(enumDef);
        defContext.afterDefInitialized(enumTypeParamDef);
//        defContext.afterDefInitialized(pEnumTypeDef);
//        TODO removed by not tested
//        defContext.addDef(new InstanceDef<>(Instance.class, StandardTypes.anyType));
//        defContext.addDef(new InstanceDef<>(ClassInstance.class, StandardTypes.anyType));
//        defContext.addDef(new InstanceDef<>(ArrayInstance.class, StandardTypes.anyType));

        primTypeFactory.saveDefs(defContext);

        primTypeFactory.getMap().keySet().forEach(javaType ->
                defContext.afterDefInitialized(defContext.getDef(javaType))
        );

        StandardTypes.setThrowableType(ClassTypeBuilder.newBuilder("中断", Throwable.class.getSimpleName())
                .source(ClassSource.BUILTIN).build());
        createThrowableFlows(StandardTypes.getThrowableType());
        var throwableDef = createValueDef(
                Throwable.class,
                Throwable.class,
                StandardTypes.getThrowableType(),
                defContext
        );
        defContext.preAddDef(throwableDef);
        primTypeFactory.addAuxType(Throwable.class, StandardTypes.getThrowableType().getType());
        var javaMessageField = ReflectionUtils.getField(Throwable.class, "detailMessage");
        /*
         Predefine composite types because the 'cause' field depends on Throwable | Null
         Do not call createCompositeTypes, it will initialize the throwable type without fields!
         */
        defContext.predefineCompositeTypes(StandardTypes.getThrowableType().getType());
        createFieldDef(
                javaMessageField,
                createField(javaMessageField, true,
                        defContext.getNullableType(StandardTypes.getStringType()), Access.PUBLIC,
                        ColumnKind.STRING.getColumn(0), StandardTypes.getThrowableType()),
                throwableDef
        );

        var javaCauseField = ReflectionUtils.getField(Throwable.class, "cause");
        createFieldDef(
                javaCauseField,
                createField(javaCauseField, false,
                        defContext.getNullableType(StandardTypes.getThrowableType().getType()), Access.PUBLIC,
                        ColumnKind.REFERENCE.getColumn(0), StandardTypes.getThrowableType()),
                throwableDef
        );
        defContext.afterDefInitialized(throwableDef);
//        defContext.initCompositeTypes(Throwable.class);

        StandardTypes.setExceptionType(ClassTypeBuilder.newBuilder("异常", Exception.class.getSimpleName())
                .superClass(StandardTypes.getThrowableType().getType())
                .source(ClassSource.BUILTIN).build());

        createExceptionFlows(StandardTypes.getExceptionType());
//        defContext.addDef(createValueDef(Exception.class, Exception.class, exceptionType, defContext));
        defContext.addDef(new DirectDef<>(Exception.class, StandardTypes.getExceptionType().getType(), ExceptionNative.class));

        StandardTypes.setRuntimeExceptionType(ClassTypeBuilder.newBuilder("运行时异常", RuntimeException.class.getSimpleName())
                .superClass(StandardTypes.getExceptionType().getType())
                .source(ClassSource.BUILTIN).build());
        createRuntimeExceptionFlows(StandardTypes.getRuntimeExceptionType());
        defContext.addDef(new DirectDef<>(
                RuntimeException.class, StandardTypes.getRuntimeExceptionType().getType(), RuntimeExceptionNative.class));

        StandardTypes.setIllegalArgumentExceptionType(ClassTypeBuilder.newBuilder("非法参数异常", IllegalArgumentException.class.getSimpleName())
                .superClass(StandardTypes.getRuntimeExceptionType().getType())
                .source(ClassSource.BUILTIN).build());
        createIllegalArgumentExceptionFlows(StandardTypes.getIllegalArgumentExceptionType());
        defContext.addDef(new DirectDef<>(
                IllegalArgumentException.class, StandardTypes.getIllegalArgumentExceptionType().getType(), IllegalArgumentExceptionNative.class));

        StandardTypes.setIllegalStateExceptionType(ClassTypeBuilder.newBuilder("非法状态异常", IllegalStateException.class.getSimpleName())
                .superClass(StandardTypes.getRuntimeExceptionType().getType())
                .source(ClassSource.BUILTIN).build());
        createIllegalStateExceptionFlows(StandardTypes.getIllegalStateExceptionType());
        defContext.addDef(new DirectDef<>(
                IllegalStateException.class, StandardTypes.getIllegalStateExceptionType().getType(), IllegalStateExceptionNative.class));

        StandardTypes.setNullPointerExceptionType(ClassTypeBuilder.newBuilder("空指针异常", NullPointerException.class.getSimpleName())
                .superClass(StandardTypes.getRuntimeExceptionType().getType())
                .source(ClassSource.BUILTIN).build());
        createNullPointerExceptionFlows(StandardTypes.getNullPointerExceptionType());
        defContext.addDef(new DirectDef<>(
                NullPointerException.class, StandardTypes.getNullPointerExceptionType().getType(), NullPointerException.class));
    }

    private Klass createConsumerType() {
        var elementType = new TypeVariable(null, "元素", "T",
                DummyGenericDeclaration.INSTANCE);
        elementType.setBounds(List.of(getObjectType()));
        primTypeFactory.putType(Consumer.class.getTypeParameters()[0], elementType.getType());
        var consumerType = ClassTypeBuilder.newBuilder("消费者", "Consumer")
                .typeParameters(elementType)
                .source(ClassSource.BUILTIN)
                .kind(ClassKind.INTERFACE)
                .build();
        primTypeFactory.putType(Consumer.class, consumerType.getType());
        MethodBuilder.newBuilder(consumerType, "消费", "accept")
                .returnType(StandardTypes.getVoidType())
                .parameters(new Parameter(null, "元素", "element", elementType.getType()))
                .build();
        return consumerType;
    }

    private Klass createPredicateType() {
        var elementType = new TypeVariable(null, "元素", "T",
                DummyGenericDeclaration.INSTANCE);
        elementType.setBounds(List.of(getObjectType()));
        primTypeFactory.putType(Predicate.class.getTypeParameters()[0], elementType.getType());
        var predicateType = ClassTypeBuilder.newBuilder("断言", "Predicate")
                .typeParameters(elementType)
                .source(ClassSource.BUILTIN)
                .kind(ClassKind.INTERFACE)
                .build();
        primTypeFactory.putType(Predicate.class, predicateType.getType());
        MethodBuilder.newBuilder(predicateType, "测试", "test")
                .returnType(StandardTypes.getBooleanType())
                .parameters(new Parameter(null, "元素", "element", elementType.getType()))
                .build();
        return predicateType;
    }

    private void initBuiltinFlows() {
        var getSourceFunc = FunctionBuilder.newBuilder("获取来源", "getSource")
                .isNative()
                .parameters(new Parameter(null, "视图", "view", getObjectType()))
                .returnType(getObjectType())
                .build();
        NativeFunctions.setGetSourceFunc(getSourceFunc);
        defContext.writeEntity(getSourceFunc);

        var setSourceFunc = new FunctionBuilder("设置来源", "setSource")
                .isNative()
                .parameters(
                        new Parameter(null, "视图", "view", getObjectType()),
                        new Parameter(null, "来源", "source", getObjectType())
                )
                .returnType(StandardTypes.getVoidType())
                .build();
        NativeFunctions.setSetSourceFunc(setSourceFunc);
        defContext.writeEntity(setSourceFunc);

        var isSourcePresentFunc = FunctionBuilder.newBuilder("来源是否存在", "isSourcePResent")
                .isNative()
                .parameters(new Parameter(null, "视图", "view", getObjectType()))
                .returnType(StandardTypes.getBooleanType())
                .build();
        NativeFunctions.setIsSourcePresent(isSourcePresentFunc);
        defContext.writeEntity(isSourcePresentFunc);

        var funcType = new TypeVariable(null, "函数类型", "FunctionType", DummyGenericDeclaration.INSTANCE);
        var function2instance = FunctionBuilder.newBuilder("函数转实例", "functionToInstance")
                .isNative()
                .typeParameters(List.of(funcType))
                .parameters(new Parameter(null, "函数", "function", StandardTypes.getAnyType()))
                .returnType(funcType.getType())
                .build();
        NativeFunctions.setFunctionToInstance(function2instance);
        defContext.writeEntity(function2instance);

        var sendEmail = FunctionBuilder.newBuilder("发送邮件", "sendEmail")
                        .isNative()
                        .parameters(
                                new Parameter(null, "收件人", "recipient", StandardTypes.getStringType()),
                                new Parameter(null, "主题", "subject", StandardTypes.getStringType()),
                                new Parameter(null, "内容", "content", StandardTypes.getStringType())
                        )
                        .returnType(StandardTypes.getVoidType())
                        .build();
        NativeFunctions.setSendEmail(sendEmail);
        defContext.writeEntity(sendEmail);

        var getSessionEntry = FunctionBuilder.newBuilder("获取会话条目", "getSessionEntry")
                .isNative()
                .parameters(
                        new Parameter(null, "键", "key", StandardTypes.getStringType())
                )
                .returnType(StandardTypes.getNullableAnyType())
                .build();
        NativeFunctions.setGetSessionEntry(getSessionEntry);
        defContext.writeEntity(getSessionEntry);

        var setSessionEntry = FunctionBuilder.newBuilder("设置会话条目", "setSessionEntry")
                .isNative()
                .parameters(
                        new Parameter(null, "键", "key", StandardTypes.getStringType()),
                        new Parameter(null, "值", "value", StandardTypes.getAnyType())
                )
                .returnType(StandardTypes.getVoidType())
                .build();
        NativeFunctions.setSetSessionEntry(setSessionEntry);
        defContext.writeEntity(setSessionEntry);

        var removeSessionEntry = FunctionBuilder.newBuilder("删除会话条目", "removeSessionEntry")
                .isNative()
                .parameters(
                        new Parameter(null, "键", "key", StandardTypes.getStringType())
                )
                .returnType(StandardTypes.getBooleanType())
                .build();
        NativeFunctions.setRemoveSessionEntry(removeSessionEntry);
        defContext.writeEntity(removeSessionEntry);

        var castedType = new TypeVariable(null, "转换类型", "CastedType", DummyGenericDeclaration.INSTANCE);
        var typeCast = FunctionBuilder.newBuilder("类型转换", "typeCast")
                .isNative()
                .typeParameters(List.of(castedType))
                .parameters(
                        new Parameter(null, "实例", "instance", StandardTypes.getNullableAnyType())
                )
                .returnType(castedType.getType())
                .build();
        NativeFunctions.setTypeCast(typeCast);
        defContext.writeEntity(typeCast);

        var print = FunctionBuilder.newBuilder("打印", "print")
                .isNative()
                .parameters(new Parameter(null, "内容", "content", StandardTypes.getNullableAnyType()))
                .returnType(StandardTypes.getVoidType())
                .build();
        NativeFunctions.setPrint(print);
        defContext.writeEntity(print);

        var delete = FunctionBuilder.newBuilder("删除", "delete")
                .isNative()
                .parameters(new Parameter(null, "实例", "instance", StandardTypes.getAnyType()))
                .returnType(StandardTypes.getVoidType())
                .build();
        NativeFunctions.setDelete(delete);
        defContext.writeEntity(delete);
    }

    @SuppressWarnings("SameParameterValue")
    private <T extends Entity> EntityDef<T> createEntityDef(java.lang.reflect.Type javaType,
                                                            Class<T> javaClass,
                                                            Klass type,
                                                            DefContext defContext) {
        return new EntityDef<>(
                javaClass,
                javaType,
                null,
                type,
                defContext
        );
    }

    @SuppressWarnings("SameParameterValue")
    private <T> ValueDef<T> createValueDef(java.lang.reflect.Type javaType,
                                           Class<T> javaClass,
                                           Klass type,
                                           DefContext defContext) {
        return new ValueDef<>(
                javaClass,
                javaType,
                null,
                type,
                defContext
        );
    }

    private tech.metavm.object.type.Field createField(Field javaField,
                                                      boolean asTitle,
                                                      Type type,
                                                      Access access,
                                                      Column column,
                                                      Klass declaringType) {
        return FieldBuilder.newBuilder(
                        EntityUtils.getMetaFieldName(javaField),
                        javaField.getName(),
                        declaringType, type)
                .column(column)
                .access(access)
                .nullType(StandardTypes.getNullType())
                .defaultValue(new NullInstance(getNullType()))
                .staticValue(new NullInstance(getNullType()))
                .build();
    }

    public PrimitiveType getNullType() {
        return primitiveTypeMap.get(Null.class);
    }

    public DirectDef<Object> getObjectDef() {
        return objectDef;
    }

    public ValueDef<Enum<?>> getEnumDef() {
        return enumDef;
    }

    private FieldDef createFieldDef(Field reflectField,
                                    tech.metavm.object.type.Field field,
                                    PojoDef<?> declaringTypeDef
    ) {
        return new FieldDef(
                field,
                false,
                reflectField,
                declaringTypeDef,
                null
        );
    }

    public PrimitiveType getStringType() {
        return primitiveTypeMap.get(String.class);
    }

    public AnyType getObjectType() {
        return (AnyType) objectDef.getType();
    }

    @SuppressWarnings("unused")
    public Type getLongDef() {
        return primitiveTypeMap.get(Long.class);
    }

    public Klass getEnumType() {
        return enumDef.getType().resolve();
    }

    public tech.metavm.object.type.Field getEnumNameField() {
        return enumNameDef.getField();
    }

    public tech.metavm.object.type.Field getEnumOrdinalField() {
        return enumOrdinalDef.getField();
    }

    public Klass createIteratorType() {
        String name = getParameterizedName("迭代器");
        String code = getParameterizedCode("Iterator");
        var elementType = new TypeVariable(null, "迭代器元素", "IteratorElement",
                DummyGenericDeclaration.INSTANCE);
        elementType.setBounds(List.of(getObjectType()));
        primTypeFactory.putType(MetaIterator.class.getTypeParameters()[0], elementType.getType());
        Klass iteratorType = ClassTypeBuilder.newBuilder(name, code)
                .typeParameters(elementType)
                .source(ClassSource.BUILTIN)
                .kind(ClassKind.INTERFACE).build();
        primTypeFactory.putType(MetaIterator.class, iteratorType.getType());
        createIteratorFlows(iteratorType, elementType.getType());
        return iteratorType;
    }

    private void createIteratorFlows(Klass iteratorType, Type elementType) {
        boolean isAbstract = iteratorType.isInterface();
        boolean isNative = !iteratorType.isInterface();
        MethodBuilder.newBuilder(iteratorType, "是否存在次项", "hasNext")
                .isNative(isNative)
                .isAbstract(isAbstract)
                .returnType(StandardTypes.getBooleanType())
                .build();

        MethodBuilder.newBuilder(iteratorType, "获取次项", "next")
                .isAbstract(isAbstract)
                .isNative(isNative)
                .returnType(elementType)
                .build();

        iteratorType.setStage(ResolutionStage.DEFINITION);
    }

    public Klass createIterableType() {
        var elementType = new TypeVariable(null, "元素", "T",
                DummyGenericDeclaration.INSTANCE);
        elementType.setBounds(List.of(getObjectType()));
        primTypeFactory.putType(MetaIterable.class.getTypeParameters()[0], elementType.getType());
        var iterableType = ClassTypeBuilder.newBuilder("可迭代", "Iterable")
                .typeParameters(elementType)
                .source(ClassSource.BUILTIN)
                .kind(ClassKind.INTERFACE)
                .build();
        primTypeFactory.putType(MetaIterable.class, iterableType.getType());
        createIterableFlows(iterableType, elementType);
        return iterableType;
    }

    private void createIterableFlows(Klass iterableType, TypeVariable elementType) {
        MethodBuilder.newBuilder(iterableType, "forEach", "forEach")
                .isNative(true)
                .returnType(StandardTypes.getVoidType())
                .parameters(new Parameter(null, "action", "action",
                        defContext.getGenericContext().getParameterizedType(
                                StandardTypes.getConsumerType(),
                                defContext.getUncertainType(elementType.getType(), StandardTypes.getNullableAnyType())
                        ).getType())
                )
                .build();

        var pIteratorType = defContext.getGenericContext().getParameterizedType(StandardTypes.getIteratorType(), elementType.getType());
        MethodBuilder.newBuilder(iterableType, "获取迭代器", "iterator")
                .isNative(true)
                .returnType(pIteratorType.getType())
                .build();

        iterableType.setStage(ResolutionStage.DEFINITION);
    }

    public Klass createCollectionType() {
        String name = getParameterizedName("Collection");
        String code = getParameterizedCode("Collection");
        var elementType = new TypeVariable(null, "Collection元素", "CollectionElement",
                DummyGenericDeclaration.INSTANCE);
        var pIterableType = defContext.getGenericContext().getParameterizedType(StandardTypes.getIterableType(), elementType.getType());
        elementType.setBounds(List.of(getObjectType()));
        primTypeFactory.putType(Collection.class.getTypeParameters()[0], elementType.getType());
        Klass collectionType = ClassTypeBuilder.newBuilder(name, code)
                .typeParameters(elementType)
                .source(ClassSource.BUILTIN)
                .interfaces(pIterableType.getType())
                .kind(ClassKind.INTERFACE)
                .build();
        primTypeFactory.putType(Collection.class, collectionType.getType());
        createCollectionFlows(collectionType, elementType);
        return collectionType;
    }

    private void createCollectionFlows(Klass collectionType, TypeVariable elementType) {
        MethodBuilder.newBuilder(collectionType, "计数", "size")
                .isNative(true)
                .returnType(StandardTypes.getLongType())
                .build();

        MethodBuilder.newBuilder(collectionType, "是否为空", "isEmpty")
                .isNative(true)
                .returnType(StandardTypes.getBooleanType())
                .build();

        MethodBuilder.newBuilder(collectionType, "是否包含", "contains")
                .isNative(true)
                .returnType(StandardTypes.getBooleanType())
                .parameters(new Parameter(null, "元素", "element", StandardTypes.getAnyType()))
                .build();

        MethodBuilder.newBuilder(collectionType, "添加", "add")
                .isNative(true)
                .returnType(StandardTypes.getBooleanType())
                .parameters(new Parameter(null, "元素", "element", elementType.getType()))
                .build();

        MethodBuilder.newBuilder(collectionType, "添加全部", "addAll")
                .isNative(true)
                .returnType(StandardTypes.getBooleanType())
                .parameters(new Parameter(null, "集合", "c",
                        defContext.getParameterizedType(collectionType, defContext.getUncertainType(StandardTypes.getNeverType(), elementType.getType())).getType()))
                .build();

        MethodBuilder.newBuilder(collectionType, "删除", "remove")
                .isNative(true)
                .returnType(StandardTypes.getBooleanType())
                .parameters(new Parameter(null, "元素", "element", StandardTypes.getAnyType()))
                .build();

        MethodBuilder.newBuilder(collectionType, "清空", "clear")
                .isNative(true)
                .returnType(StandardTypes.getVoidType())
                .build();

        MethodBuilder.newBuilder(collectionType, "条件删除", "removeIf")
                .isNative(true)
                .returnType(StandardTypes.getBooleanType())
                .parameters(new Parameter(null, "条件", "filter",
                        defContext.getGenericContext().getParameterizedType(
                                StandardTypes.getPredicateType(),
                                defContext.getUncertainType(elementType.getType(), StandardTypes.getNullableAnyType())
                        ).getType()))
                .build();

        collectionType.setStage(ResolutionStage.DEFINITION);
    }

    public Klass createSetType() {
        String name = getParameterizedName("集合");
        String code = getParameterizedCode("Set");
        var elementType = new TypeVariable(null, "集合元素", "SetElement",
                DummyGenericDeclaration.INSTANCE);
        elementType.setBounds(List.of(getObjectType()));
        primTypeFactory.putType(MetaSet.class.getTypeParameters()[0], elementType.getType());
        var pIterableType = defContext.getGenericContext().getParameterizedType(StandardTypes.getIterableType(), elementType.getType());
        var pCollectionType = defContext.getGenericContext().getParameterizedType(StandardTypes.getCollectionType(), elementType.getType());
        var pIteratorImplType = defContext.getGenericContext().getParameterizedType(StandardTypes.getIteratorImplType(), elementType.getType());
        Klass setType = ClassTypeBuilder.newBuilder(name, code)
                .interfaces(pCollectionType.getType())
                .typeParameters(elementType)
                .source(ClassSource.BUILTIN)
                .dependencies(List.of(pIteratorImplType))
                .build();
        primTypeFactory.putType(MetaSet.class, setType.getType());
        FieldBuilder.newBuilder("数组", "array", setType, defContext.getArrayType(elementType.getType(), ArrayKind.READ_WRITE))
                .nullType(getNullType())
                .access(Access.PRIVATE)
                .isChild(true)
                .nullType(StandardTypes.getNullType())
                .build();
        createSetFlows(setType, pCollectionType, pIterableType);
        return setType;
    }

    private void createSetFlows(Klass setType, /*ClassType pSetType, */Klass collectionType, Klass iterableType) {
        MethodBuilder.newBuilder(setType, "集合", "Set")
                .isConstructor(true)
                .isNative(true)
                .returnType(setType.getType())
                .build();
        createOverridingFlows(setType, collectionType);
        createOverridingFlows(setType, iterableType);
        setType.setStage(ResolutionStage.DEFINITION);
    }

    public Klass createListType() {
        var elementType = new TypeVariable(null, "列表元素",
                "ListElement",
                DummyGenericDeclaration.INSTANCE);
        elementType.setBounds(List.of(getObjectType()));
        primTypeFactory.putType(MetaList.class.getTypeParameters()[0], elementType.getType());
        var pCollectionType = defContext.getGenericContext().getParameterizedType(StandardTypes.getCollectionType(), elementType.getType());
        var pIteratorImplType = defContext.getGenericContext().getParameterizedType(StandardTypes.getIteratorImplType(), elementType.getType());
        var listType = ClassTypeBuilder.newBuilder("列表", "List")
                .kind(ClassKind.INTERFACE)
                .interfaces(pCollectionType.getType())
                .typeParameters(elementType)
                .source(ClassSource.BUILTIN)
                .dependencies(List.of(pIteratorImplType))
                .build();
        primTypeFactory.putType(MetaList.class, listType.getType());

        var nullableElementType = defContext.getUnionType(Set.of(elementType.getType(), StandardTypes.getNullType()));
        MethodBuilder.newBuilder(listType, "按索引删除", "removeAt")
                .parameters(new Parameter(null, "索引", "index", StandardTypes.getLongType()))
                .isNative(true)
                .returnType(nullableElementType)
                .build();

        MethodBuilder.newBuilder(listType, "查询", "get")
                .parameters(new Parameter(null, "索引", "index", StandardTypes.getLongType()))
                .isNative(true)
                .returnType(elementType.getType())
                .build();

        var uncertainType = defContext.getUncertainType(StandardTypes.getNeverType(), elementType.getType());
        var uncertainCollType = defContext.getParameterizedType(StandardTypes.getCollectionType(), uncertainType);
        MethodBuilder.newBuilder(listType, listType.getName(), listType.getCode())
                .isConstructor(true)
                .isNative(true)
                .parameters(
                        new Parameter(null, "collection", "collection", uncertainCollType.getType())
                )
                .returnType(listType.getType())
                .build();

        MethodBuilder.newBuilder(listType, "写入", "set")
                .parameters(
                        new Parameter(null, "索引", "index", StandardTypes.getLongType()),
                        new Parameter(null, "值", "value", elementType.getType())
                )
                .isNative(true)
                .returnType(nullableElementType)
                .build();

        MethodBuilder.newBuilder(listType, "of", "of")
                .isStatic(true)
                .isNative(true)
                .parameters(
                        new Parameter(null, "元素", "elements", defContext.getArrayType(elementType.getType(), ArrayKind.READ_ONLY))
                )
                .returnType(listType.getType())
                .build();

        listType.setStage(ResolutionStage.DEFINITION);
        return listType;
    }

    public Klass createReadWriteListType() {
        return createListImplType("读写列表", "ReadWriteList", ReadWriteMetaList.class, ArrayKind.READ_WRITE);
    }

    public Klass createChildListType() {
        return createListImplType("子对象列表", "ChildList", ChildMetaList.class, ArrayKind.CHILD);
    }

    public Klass createListImplType(String name, String code, Class<?> javaClass, ArrayKind arrayKind) {
        var elementType = new TypeVariable(null, name + "元素", code + "Element",
                DummyGenericDeclaration.INSTANCE);
        elementType.setBounds(List.of(getObjectType()));
        primTypeFactory.putType(javaClass.getTypeParameters()[0], elementType.getType());
        var pIterableType = defContext.getGenericContext().getParameterizedType(StandardTypes.getIterableType(), elementType.getType());
        var pCollectionType = defContext.getGenericContext().getParameterizedType(StandardTypes.getCollectionType(), elementType.getType());
        var pListType = defContext.getGenericContext().getParameterizedType(StandardTypes.getListType(), elementType.getType());
        var pIteratorImplType = defContext.getGenericContext().getParameterizedType(StandardTypes.getIteratorImplType(), elementType.getType());
        var listImplType = ClassTypeBuilder.newBuilder(name, code)
                .interfaces(pListType.getType())
                .typeParameters(elementType)
                .source(ClassSource.BUILTIN)
                .dependencies(List.of(pIteratorImplType))
                .build();
        primTypeFactory.putType(javaClass, listImplType.getType());
        FieldBuilder.newBuilder("数组", "array", listImplType,
                        defContext.getArrayType(elementType.getType(), arrayKind))
                .nullType(getNullType())
                .access(Access.PRIVATE)
                .isChild(true)
                .build();
        createOverridingFlows(listImplType, pIterableType);
        createOverridingFlows(listImplType, pCollectionType);
        createOverridingFlows(listImplType, pListType);

        MethodBuilder.newBuilder(listImplType, listImplType.getName(), listImplType.getCode())
                .isConstructor(true)
                .isNative(true)
                .returnType(listImplType.getType())
                .build();


        MethodBuilder.newBuilder(listImplType, listImplType.getName(), listImplType.getCode())
                .isConstructor(true)
                .isNative(true)
                .parameters(
                        new Parameter(
                                null, "collection", "collection",
                                defContext.getParameterizedType(
                                        StandardTypes.getCollectionType(),
                                        defContext.getUncertainTypeContext().get(StandardTypes.getNeverType(), elementType.getType())
                                ).getType()
                        )
                )
                .returnType(listImplType.getType())
                .build();
        return listImplType;
    }

    private void createCommonListFlows(Klass listType, TypeVariable elementType) {
        MethodBuilder.newBuilder(listType, listType.getName(), listType.getCode())
                .isConstructor(true)
                .isNative(true)
                .returnType(listType.getType())
                .build();
    }

    private void createOrdinaryListFlows(Klass listType, TypeVariable elementType, Klass pCollectionType) {
        createOverridingFlows(listType, pCollectionType);

        var uncertainType = defContext.getUncertainType(StandardTypes.getNeverType(), elementType.getType());
        var uncertainCollType = defContext.getParameterizedType(StandardTypes.getCollectionType(), uncertainType);
        MethodBuilder.newBuilder(listType, listType.getName(), listType.getCode())
                .isConstructor(true)
                .isNative(true)
                .parameters(
                        new Parameter(null, "collection", "collection", uncertainCollType.getType())
                )
                .returnType(listType.getType())
                .build();

        var nullableElementType = defContext.getUnionType(Set.of(elementType.getType(), StandardTypes.getNullType()));
        MethodBuilder.newBuilder(listType, "写入", "set")
                .parameters(
                        new Parameter(null, "索引", "index", StandardTypes.getLongType()),
                        new Parameter(null, "值", "value", elementType.getType())
                )
                .isNative(true)
                .returnType(nullableElementType)
                .build();

        listType.setStage(ResolutionStage.DEFINITION);
    }

    public Klass createIteratorImplType() {
        String name = getParameterizedName("迭代器实现");
        String code = getParameterizedCode("IteratorImpl");
        var elementType = new TypeVariable(null, "迭代器实现元素", "IteratorImplElement",
                DummyGenericDeclaration.INSTANCE);
        elementType.setBounds(List.of(getObjectType()));
        primTypeFactory.putType(IteratorImpl.class.getTypeParameters()[0], elementType.getType());
        var pIteratorType = defContext.getGenericContext().getParameterizedType(StandardTypes.getIteratorType(), elementType.getType());
        Klass iteratorImplType = ClassTypeBuilder.newBuilder(name, code)
                .interfaces(List.of(pIteratorType.getType()))
                .typeParameters(elementType)
                .source(ClassSource.BUILTIN)
                .build();
        primTypeFactory.putType(IteratorImpl.class, iteratorImplType.getType());
        var pCollectionType = defContext.getGenericContext().getParameterizedType(StandardTypes.getCollectionType(), elementType.getType());
        MethodBuilder.newBuilder(iteratorImplType, "IteratorImpl", "IteratorImpl")
                .isConstructor(true)
                .isNative(true)
                .returnType(iteratorImplType.getType())
                .parameters(new Parameter(null, "集合", "collection", pCollectionType.getType()))
                .build();
        createOverridingFlows(iteratorImplType, pIteratorType);
        iteratorImplType.setStage(ResolutionStage.DEFINITION);
        return iteratorImplType;
    }

    private void createOverridingFlows(Klass declaringType, Klass baseType) {
        for (Method flow : baseType.getMethods()) {
            MethodBuilder.newBuilder(declaringType, flow.getName(), flow.getCode())
                    .isNative(true)
                    .access(flow.getAccess())
                    .overridden(List.of(flow))
                    .returnType(flow.getReturnType())
                    .parameters(NncUtils.map(flow.getParameters(), Parameter::copy))
                    .typeParameters(NncUtils.map(flow.getTypeParameters(), TypeVariable::copy))
                    .build();
        }
    }

    public Klass createMapType() {
        String name = getParameterizedName("词典");
        String code = getParameterizedName("Map");
        var keyType = new TypeVariable(null, "词典键", "MapKey",
                DummyGenericDeclaration.INSTANCE);
        keyType.setBounds(List.of(getObjectType()));
        primTypeFactory.putType(MetaMap.class.getTypeParameters()[0], keyType.getType());
        var valueType = new TypeVariable(null, "词典值", "MapValue",
                DummyGenericDeclaration.INSTANCE);
        valueType.setBounds(List.of(getObjectType()));
        primTypeFactory.putType(MetaMap.class.getTypeParameters()[1], valueType.getType());
        var pSetType = defContext.getGenericContext().getParameterizedType(StandardTypes.getSetType(), keyType.getType());
        Klass mapType = ClassTypeBuilder.newBuilder(name, code)
                .source(ClassSource.BUILTIN)
                .dependencies(List.of(pSetType))
                .typeParameters(keyType, valueType)
                .build();
        primTypeFactory.putType(MetaMap.class, mapType.getType());
        FieldBuilder
                .newBuilder("键数组", "keyArray", mapType, defContext.getArrayType(keyType.getType(), ArrayKind.READ_WRITE))
                .access(Access.PRIVATE)
                .isChild(true)
                .nullType(getNullType())
                .build();
        FieldBuilder
                .newBuilder("值数组", "valueArray", mapType, defContext.getArrayType(valueType.getType(), ArrayKind.READ_WRITE))
                .access(Access.PRIVATE)
                .isChild(true)
                .nullType(getNullType())
                .build();
        createMapFlows(mapType, keyType.getType(), valueType.getType());
        return mapType;
    }

    private void createMapFlows(Klass mapType, Type keyType, Type valueType) {
        MethodBuilder.newBuilder(mapType, "词典", "Map")
                .isConstructor(true)
                .isNative(true)
                .returnType(mapType.getType())
                .build();

        var nullableValueType = defContext.getUnionType(Set.of(valueType, StandardTypes.getNullType()));

        MethodBuilder.newBuilder(mapType, "添加", "put")
                .isNative(true)
                .returnType(nullableValueType)
                .parameters(new Parameter(null, "键", "key", keyType),
                        new Parameter(null, "值", "value", valueType))
                .build();

        MethodBuilder.newBuilder(mapType, "查询", "get")
                .isNative(true)
                .returnType(nullableValueType)
                .parameters(new Parameter(null, "键", "key", keyType))
                .build();

        MethodBuilder.newBuilder(mapType, "删除", "remove")
                .isNative(true)
                .returnType(nullableValueType)
                .parameters(new Parameter(null, "键", "key", keyType))
                .build();

        MethodBuilder.newBuilder(mapType, "计数", "size")
                .isNative(true)
                .returnType(StandardTypes.getLongType())
                .build();

        MethodBuilder.newBuilder(mapType, "清空", "clear")
                .isNative(true)
                .returnType(StandardTypes.getVoidType())
                .build();
        mapType.setStage(ResolutionStage.DEFINITION);
    }

    private void createThrowableFlows(Klass throwableType) {
        MethodBuilder.newBuilder(throwableType, "Throwable", "Throwable")
                .isConstructor(true)
                .isNative(true)
                .returnType(throwableType.getType())
                .build();

        MethodBuilder.newBuilder(throwableType, "Throwable", "Throwable")
                .isConstructor(true)
                .isNative(true)
                .returnType(throwableType.getType())
                .parameters(new Parameter(null, "错误详情", "message", StandardTypes.getStringType()))
                .build();

        MethodBuilder.newBuilder(throwableType, "Throwable", "Throwable")
                .isConstructor(true)
                .isNative(true)
                .returnType(throwableType.getType())
                .parameters(new Parameter(null, "原因", "cause", throwableType.getType()))
                .build();

        MethodBuilder.newBuilder(throwableType, "Throwable", "Throwable")
                .isConstructor(true)
                .isNative(true)
                .returnType(throwableType.getType())
                .parameters(
                        new Parameter(null, "错误详情", "message", StandardTypes.getStringType()),
                        new Parameter(null, "原因", "cause", throwableType.getType())
                )
                .build();

        MethodBuilder.newBuilder(throwableType, "获取详情", "getMessage")
                .isNative(true)
                .returnType(defContext.getNullableType(StandardTypes.getStringType()))
                .build();
    }

    private void createExceptionFlows(Klass exceptionType) {
        createExceptionFlows("Exception", "Exception", exceptionType);
    }

    private void createRuntimeExceptionFlows(Klass exceptionType) {
        createExceptionFlows("RuntimeException", "RuntimeException", exceptionType);
    }

    private void createIllegalArgumentExceptionFlows(Klass exceptionType) {
        createExceptionFlows("IllegalArgumentException", "IllegalArgumentException", exceptionType);
    }

    private void createIllegalStateExceptionFlows(Klass exceptionType) {
        createExceptionFlows("IllegalStateException", "IllegalStateException", exceptionType);
    }

    private void createNullPointerExceptionFlows(Klass exceptionType) {
        createExceptionFlows("NullPointerException", "NullPointerException", exceptionType);
    }

    private void createExceptionFlows(String name, String code, Klass runtimeExceptionType) {
        MethodBuilder.newBuilder(runtimeExceptionType, name, code)
                .isConstructor(true)
                .isNative(true)
                .returnType(runtimeExceptionType.getType())
                .build();

        MethodBuilder.newBuilder(runtimeExceptionType, name, code)
                .isConstructor(true)
                .isNative(true)
                .returnType(runtimeExceptionType.getType())
                .parameters(new Parameter(null, "错误详情", "message", StandardTypes.getStringType()))
                .build();

        MethodBuilder.newBuilder(runtimeExceptionType, name, code)
                .isConstructor(true)
                .isNative(true)
                .returnType(runtimeExceptionType.getType())
                .parameters(new Parameter(null, "原因", "cause", StandardTypes.getThrowableType().getType()))
                .build();

        MethodBuilder.newBuilder(runtimeExceptionType, name, code)
                .isConstructor(true)
                .isNative(true)
                .returnType(runtimeExceptionType.getType())
                .parameters(
                        new Parameter(null, "错误详情", "message", StandardTypes.getStringType()),
                        new Parameter(null, "原因", "cause", StandardTypes.getThrowableType().getType())
                )
                .build();
    }


    private static class PrimTypeFactory extends TypeFactory {

        private final Map<java.lang.reflect.Type, Type> javaType2Type = new HashMap<>();
        private final Map<Type, java.lang.reflect.Type> type2JavaType = new IdentityHashMap<>();

        private final Map<java.lang.reflect.Type, Type> auxJavaType2Type = new HashMap<>();
        private final Map<Type, java.lang.reflect.Type> axuType2Java2Type = new IdentityHashMap<>();

        @Override
        public boolean isPutTypeSupported() {
            return true;
        }

        @Override
        public void putType(java.lang.reflect.Type javaType, Type type) {
            NncUtils.requireFalse(javaType2Type.containsKey(javaType));
            NncUtils.requireFalse(type2JavaType.containsKey(type));
            javaType2Type.put(javaType, type);
            type2JavaType.put(type, javaType);
        }

        @Override
        public java.lang.reflect.Type getJavaType(Type type) {
            return type2JavaType.getOrDefault(type, axuType2Java2Type.get(type));
        }

        public void addAuxType(java.lang.reflect.Type javaType, Type type) {
            axuType2Java2Type.put(type, javaType);
        }

        public Map<java.lang.reflect.Type, Type> getMap() {
            return Collections.unmodifiableMap(javaType2Type);
        }

        public void saveDefs(DefMap defMap) {
            for (Type type : javaType2Type.values()) {
                createDefIfAbsent(type, defMap);
            }
            for (Type type : javaType2Type.values()) {
                defMap.afterDefInitialized(defMap.getDef(type));
            }
        }

        private ModelDef<?, ?> createDefIfAbsent(Type type, DefMap defMap) {
            if (defMap.containsDef(type)) {
                return defMap.getDef(type);
            }
            var javaType = NncUtils.requireNonNull(type2JavaType.get(type));
            var def = switch (type) {
                case VariableType variableType -> new TypeVariableDef(
                        (java.lang.reflect.TypeVariable<?>) javaType, variableType.getVariable()
                );
                default -> new DirectDef<>(javaType, type, NATIVE_CLASS_MAP.get(javaType));
            };
            defMap.preAddDef(def);
            return def;
        }


    }

}
