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

    private static final Map<java.lang.reflect.Type, Class<?>> NATIVE_CLASS_MAP = Map.of(
            MetaSet.class, SetNative.class,
            ChildMetaList.class, ListNative.class,
            ReadWriteMetaList.class, ListNative.class,
            MetaList.class, ListNative.class,
            MetaMap.class, MapNative.class,
            IteratorImpl.class, IteratorImplNative.class,
            Throwable.class, ThrowableNative.class,
            Exception.class, ExceptionNative.class,
            RuntimeException.class, RuntimeExceptionNative.class
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

        initBuiltinFlows();

        var collectionTypeMap = new LinkedHashMap<Class<?>, ClassType>();
        collectionTypeMap.put(MetaIterator.class, StandardTypes.setIteratorType(createIteratorType()));
        collectionTypeMap.put(Collection.class, StandardTypes.setCollectionType(createCollectionType()));
        collectionTypeMap.put(IteratorImpl.class, StandardTypes.setIteratorImplType(createIteratorImplType()));
        collectionTypeMap.put(MetaSet.class, StandardTypes.setSetType(createSetType()));
        collectionTypeMap.put(MetaList.class, StandardTypes.setListType(createListType()));
        collectionTypeMap.put(ReadWriteMetaList.class, StandardTypes.setReadWriteListType(createReadWriteListType()));
        collectionTypeMap.put(ChildMetaList.class, StandardTypes.setChildListType(createChildListType()));
        collectionTypeMap.put(MetaMap.class, StandardTypes.setMapType(createMapType()));

        defContext.addDef(objectDef);
        defContext.createCompositeTypes(StandardTypes.getAnyType());
        StandardTypes.setNullableAnyType(defContext.getNullableType(StandardTypes.getAnyType()));
        StandardTypes.setAnyArrayType(defContext.getArrayType(StandardTypes.getAnyType(), ArrayKind.READ_WRITE));
        StandardTypes.setReadonlyAnyArrayType(defContext.getArrayType(StandardTypes.getAnyType(), ArrayKind.READ_ONLY));
        StandardTypes.setNeverArrayType(defContext.getArrayType(StandardTypes.getNeverType(), ArrayKind.READ_WRITE));

        for (var entry : primitiveTypeMap.entrySet()) {
            var primClass = entry.getKey();
            var primType = entry.getValue();
            if (!primType.isNull() && !primType.isVoid()) {
                defContext.createCompositeTypes(primType);
                collectionTypeMap.forEach((collClass, collType) -> {
                    if (collClass != MetaMap.class) {
                        defContext.getGenericContext().getParameterizedType(collType, primType);
//                        primTypeFactory.putType(ParameterizedTypeImpl.create(collClass, primClass), pType);
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
                        .category(TypeCategory.VALUE).build()),
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
        primTypeFactory.addAuxType(Enum.class.getTypeParameters()[0], enumTypeParam);
        StandardTypes.setEnumType(ClassTypeBuilder.newBuilder("枚举", Enum.class.getSimpleName())
                .source(ClassSource.BUILTIN)
                .typeParameters(enumTypeParam)
                .build());
        primTypeFactory.addAuxType(Enum.class, StandardTypes.getEnumType());

//        var pEnumType = defContext.getGenericContext().getParameterizedType(enumType, enumTypeParam);
        enumTypeParam.setBounds(List.of(StandardTypes.getEnumType()));

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
        primTypeFactory.addAuxType(Throwable.class, StandardTypes.getThrowableType());
        var javaMessageField = ReflectionUtils.getField(Throwable.class, "detailMessage");
        /*
         Predefine composite types because the 'cause' field depends on Throwable | Null
         Do not call createCompositeTypes, it will initialize the throwable type without fields!
         */
        defContext.predefineCompositeTypes(StandardTypes.getThrowableType());
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
                        defContext.getNullableType(StandardTypes.getThrowableType()), Access.PUBLIC,
                        ColumnKind.REFERENCE.getColumn(0), StandardTypes.getThrowableType()),
                throwableDef
        );
        defContext.afterDefInitialized(throwableDef);
//        defContext.initCompositeTypes(Throwable.class);

        StandardTypes.setExceptionType(ClassTypeBuilder.newBuilder("异常", Exception.class.getSimpleName())
                .superClass(StandardTypes.getThrowableType())
                .source(ClassSource.BUILTIN).build());

        createExceptionFlows(StandardTypes.getExceptionType());
//        defContext.addDef(createValueDef(Exception.class, Exception.class, exceptionType, defContext));
        defContext.addDef(new DirectDef<>(Exception.class, StandardTypes.getExceptionType(), ExceptionNative.class));

        StandardTypes.setRuntimeExceptionType(ClassTypeBuilder.newBuilder("运行时异常", RuntimeException.class.getSimpleName())
                .superClass(StandardTypes.getExceptionType())
                .source(ClassSource.BUILTIN).build());
        createRuntimeExceptionFlows(StandardTypes.getRuntimeExceptionType());
        defContext.addDef(new DirectDef<>(
                RuntimeException.class, StandardTypes.getRuntimeExceptionType(), RuntimeExceptionNative.class));
    }

    private void initBuiltinFlows() {
        var getSourceFunc = FunctionBuilder.newBuilder("获取来源", "getSource", defContext.getFunctionTypeContext())
                .isNative()
                .parameters(new Parameter(null, "视图", "view", getObjectType()))
                .returnType(getObjectType())
                .build();
        NativeFunctions.setGetSourceFunc(getSourceFunc);
        defContext.writeEntity(getSourceFunc);

        var setSourceFunc = new FunctionBuilder("设置来源", "setSource", defContext.getFunctionTypeContext())
                .isNative()
                .parameters(
                        new Parameter(null, "视图", "view", getObjectType()),
                        new Parameter(null, "来源", "source", getObjectType())
                )
                .returnType(StandardTypes.getVoidType())
                .build();
        NativeFunctions.setSetSourceFunc(setSourceFunc);
        defContext.writeEntity(setSourceFunc);

        var isSourcePresentFunc = FunctionBuilder.newBuilder("来源是否存在", "isSourcePResent", defContext.getFunctionTypeContext())
                .isNative()
                .parameters(new Parameter(null, "视图", "view", getObjectType()))
                .returnType(StandardTypes.getBooleanType())
                .build();
        NativeFunctions.setIsSourcePresent(isSourcePresentFunc);
        defContext.writeEntity(isSourcePresentFunc);
    }

    @SuppressWarnings("SameParameterValue")
    private <T extends Entity> EntityDef<T> createEntityDef(java.lang.reflect.Type javaType,
                                                            Class<T> javaClass,
                                                            ClassType type,
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
                                           ClassType type,
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
                                                      ClassType declaringType) {
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

    public ClassType getEnumType() {
        return enumDef.getType();
    }

    public tech.metavm.object.type.Field getEnumNameField() {
        return enumNameDef.getField();
    }

    public tech.metavm.object.type.Field getEnumOrdinalField() {
        return enumOrdinalDef.getField();
    }

    public ClassType createIteratorType() {
        String name = getParameterizedName("迭代器");
        String code = getParameterizedCode("Iterator");
        var elementType = new TypeVariable(null, "迭代器元素", "IteratorElement",
                DummyGenericDeclaration.INSTANCE);
        elementType.setBounds(List.of(getObjectType()));
        primTypeFactory.putType(MetaIterator.class.getTypeParameters()[0], elementType);
        ClassType iteratorType = ClassTypeBuilder.newBuilder(name, code)
                .typeParameters(elementType)
                .source(ClassSource.BUILTIN)
                .category(TypeCategory.INTERFACE).build();
        primTypeFactory.putType(MetaIterator.class, iteratorType);
        createIteratorFlows(iteratorType, elementType);
        return iteratorType;
    }

    private void createIteratorFlows(ClassType iteratorType, Type elementType) {
        boolean isAbstract = iteratorType.isInterface();
        boolean isNative = !iteratorType.isInterface();
        MethodBuilder.newBuilder(iteratorType, "是否存在次项", "hasNext", defContext.getFunctionTypeContext())
                .isNative(isNative)
                .isAbstract(isAbstract)
                .returnType(StandardTypes.getBooleanType())
                .build();

        MethodBuilder.newBuilder(iteratorType, "获取次项", "next", defContext.getFunctionTypeContext())
                .isAbstract(isAbstract)
                .isNative(isNative)
                .returnType(elementType)
                .build();

        iteratorType.setStage(ResolutionStage.DEFINITION);
    }


    public ClassType createCollectionType() {
        String name = getParameterizedName("Collection");
        String code = getParameterizedCode("Collection");
        var elementType = new TypeVariable(null, "Collection元素", "CollectionElement",
                DummyGenericDeclaration.INSTANCE);
        elementType.setBounds(List.of(getObjectType()));
        primTypeFactory.putType(Collection.class.getTypeParameters()[0], elementType);
        ClassType collectionType = ClassTypeBuilder.newBuilder(name, code)
                .typeParameters(elementType)
                .source(ClassSource.BUILTIN)
                .category(TypeCategory.INTERFACE)
                .build();
        primTypeFactory.putType(Collection.class, collectionType);
        createCollectionFlows(collectionType, elementType);
        return collectionType;
    }

    private void createCollectionFlows(ClassType collectionType, TypeVariable elementType) {
        var pIteratorType = defContext.getGenericContext().getParameterizedType(StandardTypes.getIteratorType(), elementType);
        MethodBuilder.newBuilder(collectionType, "获取迭代器", "iterator", defContext.getFunctionTypeContext())
                .isNative(true)
                .returnType(pIteratorType)
                .build();

        MethodBuilder.newBuilder(collectionType, "计数", "size", defContext.getFunctionTypeContext())
                .isNative(true)
                .returnType(StandardTypes.getLongType())
                .build();

        MethodBuilder.newBuilder(collectionType, "是否为空", "isEmpty", defContext.getFunctionTypeContext())
                .isNative(true)
                .returnType(StandardTypes.getBooleanType())
                .build();

        MethodBuilder.newBuilder(collectionType, "是否包含", "contains", defContext.getFunctionTypeContext())
                .isNative(true)
                .returnType(StandardTypes.getBooleanType())
                .parameters(new Parameter(null, "元素", "element", StandardTypes.getAnyType()))
                .build();

        MethodBuilder.newBuilder(collectionType, "添加", "add", defContext.getFunctionTypeContext())
                .isNative(true)
                .returnType(StandardTypes.getBooleanType())
                .parameters(new Parameter(null, "元素", "element", elementType))
                .build();

        MethodBuilder.newBuilder(collectionType, "添加全部", "addAll", defContext.getFunctionTypeContext())
                .isNative(true)
                .returnType(StandardTypes.getBooleanType())
                .parameters(new Parameter(null, "集合", "c",
                        defContext.getParameterizedType(collectionType, defContext.getUncertainType(StandardTypes.getNeverType(), elementType))))
                .build();

        MethodBuilder.newBuilder(collectionType, "删除", "remove", defContext.getFunctionTypeContext())
                .isNative(true)
                .returnType(StandardTypes.getBooleanType())
                .parameters(new Parameter(null, "元素", "element", StandardTypes.getAnyType()))
                .build();

        MethodBuilder.newBuilder(collectionType, "清空", "clear", defContext.getFunctionTypeContext())
                .isNative(true)
                .returnType(StandardTypes.getVoidType())
                .build();

        collectionType.setStage(ResolutionStage.DEFINITION);
    }

    public ClassType createSetType() {
        String name = getParameterizedName("集合");
        String code = getParameterizedCode("Set");
        var elementType = new TypeVariable(null, "集合元素", "SetElement",
                DummyGenericDeclaration.INSTANCE);
        elementType.setBounds(List.of(getObjectType()));
        primTypeFactory.putType(MetaSet.class.getTypeParameters()[0], elementType);
        var pCollectionType = defContext.getGenericContext().getParameterizedType(StandardTypes.getCollectionType(), elementType);
        var pIteratorImplType = defContext.getGenericContext().getParameterizedType(StandardTypes.getIteratorImplType(), elementType);
        ClassType setType = ClassTypeBuilder.newBuilder(name, code)
                .interfaces(pCollectionType)
                .typeParameters(elementType)
                .source(ClassSource.BUILTIN)
                .dependencies(List.of(pIteratorImplType))
                .build();
        primTypeFactory.putType(MetaSet.class, setType);
        FieldBuilder.newBuilder("数组", "array", setType, defContext.getArrayType(elementType, ArrayKind.READ_WRITE))
                .nullType(getNullType())
                .access(Access.PRIVATE)
                .isChild(true)
                .nullType(StandardTypes.getNullType())
                .build();
        createSetFlows(setType, pCollectionType);
        return setType;
    }

    private void createSetFlows(ClassType setType, /*ClassType pSetType, */ClassType collectionType) {
        MethodBuilder.newBuilder(setType, "集合", "Set", defContext.getFunctionTypeContext())
                .isConstructor(true)
                .isNative(true)
                .returnType(setType)
                .build();
        createOverridingFlows(setType, collectionType);
        setType.setStage(ResolutionStage.DEFINITION);
    }

    public ClassType createListType() {
        var elementType = new TypeVariable(null, "列表元素",
                "ListElement",
                DummyGenericDeclaration.INSTANCE);
        elementType.setBounds(List.of(getObjectType()));
        primTypeFactory.putType(MetaList.class.getTypeParameters()[0], elementType);
        var pCollectionType = defContext.getGenericContext().getParameterizedType(StandardTypes.getCollectionType(), elementType);
        var pIteratorImplType = defContext.getGenericContext().getParameterizedType(StandardTypes.getIteratorImplType(), elementType);
        var listType = ClassTypeBuilder.newBuilder("列表", "List")
                .category(TypeCategory.INTERFACE)
                .interfaces(pCollectionType)
                .typeParameters(elementType)
                .source(ClassSource.BUILTIN)
                .dependencies(List.of(pIteratorImplType))
                .build();
        primTypeFactory.putType(MetaList.class, listType);

        var nullableElementType = defContext.getUnionType(Set.of(elementType, StandardTypes.getNullType()));
        MethodBuilder.newBuilder(listType, "按索引删除", "removeAt", defContext.getFunctionTypeContext())
                .parameters(new Parameter(null, "索引", "index", StandardTypes.getLongType()))
                .isNative(true)
                .returnType(nullableElementType)
                .build();

        MethodBuilder.newBuilder(listType, "查询", "get", defContext.getFunctionTypeContext())
                .parameters(new Parameter(null, "索引", "index", StandardTypes.getLongType()))
                .isNative(true)
                .returnType(nullableElementType)
                .build();

        var uncertainType = defContext.getUncertainType(StandardTypes.getNeverType(), elementType);
        var uncertainCollType = defContext.getParameterizedType(StandardTypes.getCollectionType(), uncertainType);
        MethodBuilder.newBuilder(listType, listType.getName(), listType.getCode(), defContext.getFunctionTypeContext())
                .isConstructor(true)
                .isNative(true)
                .parameters(
                        new Parameter(null, "collection", "collection", uncertainCollType)
                )
                .returnType(listType)
                .build();

        MethodBuilder.newBuilder(listType, "写入", "set", defContext.getFunctionTypeContext())
                .parameters(
                        new Parameter(null, "索引", "index", StandardTypes.getLongType()),
                        new Parameter(null, "值", "value", elementType)
                )
                .isNative(true)
                .returnType(nullableElementType)
                .build();

        MethodBuilder.newBuilder(listType, "of", "of", defContext.getFunctionTypeContext())
                .isStatic(true)
                .isNative(true)
                .parameters(
                        new Parameter(null, "元素", "elements", defContext.getArrayType(elementType, ArrayKind.READ_ONLY))
                )
                .returnType(listType)
                .build();

        listType.setStage(ResolutionStage.DEFINITION);
        return listType;
    }

    public ClassType createReadWriteListType() {
        return createListImplType("读写列表", "ReadWriteList", ReadWriteMetaList.class, ArrayKind.READ_WRITE);
    }

    public ClassType createChildListType() {
        return createListImplType("子对象列表", "ChildList", ChildMetaList.class, ArrayKind.CHILD);
    }

    public ClassType createListImplType(String name, String code, Class<?> javaClass, ArrayKind arrayKind) {
        var elementType = new TypeVariable(null, name + "元素", code + "Element",
                DummyGenericDeclaration.INSTANCE);
        elementType.setBounds(List.of(getObjectType()));
        primTypeFactory.putType(javaClass.getTypeParameters()[0], elementType);
        var pCollectionType = defContext.getGenericContext().getParameterizedType(StandardTypes.getCollectionType(), elementType);
        var pListType = defContext.getGenericContext().getParameterizedType(StandardTypes.getListType(), elementType);
        var pIteratorImplType = defContext.getGenericContext().getParameterizedType(StandardTypes.getIteratorImplType(), elementType);
        var listImplType = ClassTypeBuilder.newBuilder(name, code)
                .interfaces(pListType)
                .typeParameters(elementType)
                .source(ClassSource.BUILTIN)
                .dependencies(List.of(pIteratorImplType))
                .build();
        primTypeFactory.putType(javaClass, listImplType);
        FieldBuilder.newBuilder("数组", "array", listImplType,
                        defContext.getArrayType(elementType, arrayKind))
                .nullType(getNullType())
                .access(Access.PRIVATE)
                .isChild(true)
                .build();
        createOverridingFlows(listImplType, pCollectionType);
        createOverridingFlows(listImplType, pListType);

        MethodBuilder.newBuilder(listImplType, listImplType.getName(), listImplType.getCode(), defContext.getFunctionTypeContext())
                .isConstructor(true)
                .isNative(true)
                .returnType(listImplType)
                .build();


        MethodBuilder.newBuilder(listImplType, listImplType.getName(), listImplType.getCode(), defContext.getFunctionTypeContext())
                .isConstructor(true)
                .isNative(true)
                .parameters(
                        new Parameter(
                                null, "collection", "collection",
                                defContext.getParameterizedType(
                                        StandardTypes.getCollectionType(),
                                        defContext.getUncertainTypeContext().get(StandardTypes.getNeverType(), elementType)
                                )
                        )
                )
                .returnType(listImplType)
                .build();
        return listImplType;
    }

    private void createCommonListFlows(ClassType listType, TypeVariable elementType) {
        MethodBuilder.newBuilder(listType, listType.getName(), listType.getCode(), defContext.getFunctionTypeContext())
                .isConstructor(true)
                .isNative(true)
                .returnType(listType)
                .build();
    }

    private void createOrdinaryListFlows(ClassType listType, TypeVariable elementType, ClassType pCollectionType) {
        createOverridingFlows(listType, pCollectionType);

        var uncertainType = defContext.getUncertainType(StandardTypes.getNeverType(), elementType);
        var uncertainCollType = defContext.getParameterizedType(StandardTypes.getCollectionType(), uncertainType);
        MethodBuilder.newBuilder(listType, listType.getName(), listType.getCode(), defContext.getFunctionTypeContext())
                .isConstructor(true)
                .isNative(true)
                .parameters(
                        new Parameter(null, "collection", "collection", uncertainCollType)
                )
                .returnType(listType)
                .build();

        var nullableElementType = defContext.getUnionType(Set.of(elementType, StandardTypes.getNullType()));
        MethodBuilder.newBuilder(listType, "写入", "set", defContext.getFunctionTypeContext())
                .parameters(
                        new Parameter(null, "索引", "index", StandardTypes.getLongType()),
                        new Parameter(null, "值", "value", elementType)
                )
                .isNative(true)
                .returnType(nullableElementType)
                .build();

        listType.setStage(ResolutionStage.DEFINITION);
    }

    public ClassType createIteratorImplType() {
        String name = getParameterizedName("迭代器实现");
        String code = getParameterizedCode("IteratorImpl");
        var elementType = new TypeVariable(null, "迭代器实现元素", "IteratorImplElement",
                DummyGenericDeclaration.INSTANCE);
        elementType.setBounds(List.of(getObjectType()));
        primTypeFactory.putType(IteratorImpl.class.getTypeParameters()[0], elementType);
        var pIteratorType = defContext.getGenericContext().getParameterizedType(StandardTypes.getIteratorType(), elementType);
        ClassType iteratorImplType = ClassTypeBuilder.newBuilder(name, code)
                .interfaces(List.of(pIteratorType))
                .typeParameters(elementType)
                .source(ClassSource.BUILTIN)
                .build();
        primTypeFactory.putType(IteratorImpl.class, iteratorImplType);
        var pCollectionType = defContext.getGenericContext().getParameterizedType(StandardTypes.getCollectionType(), elementType);
        MethodBuilder.newBuilder(iteratorImplType, "IteratorImpl", "IteratorImpl", defContext.getFunctionTypeContext())
                .isConstructor(true)
                .isNative(true)
                .returnType(iteratorImplType)
                .parameters(new Parameter(null, "集合", "collection", pCollectionType))
                .build();
        createOverridingFlows(iteratorImplType, pIteratorType);
        iteratorImplType.setStage(ResolutionStage.DEFINITION);
        return iteratorImplType;
    }

    private void createOverridingFlows(ClassType declaringType, ClassType baseType) {
        for (Method flow : baseType.getMethods()) {
            MethodBuilder.newBuilder(declaringType, flow.getName(), flow.getCode(), defContext.getFunctionTypeContext())
                    .isNative(true)
                    .access(flow.getAccess())
                    .overridden(List.of(flow))
                    .returnType(flow.getReturnType())
                    .parameters(NncUtils.map(flow.getParameters(), Parameter::copy))
                    .typeParameters(NncUtils.map(flow.getTypeParameters(), TypeVariable::copy))
                    .build();
        }
    }

    public ClassType createMapType() {
        String name = getParameterizedName("词典");
        String code = getParameterizedName("Map");
        var keyType = new TypeVariable(null, "词典键", "MapKey",
                DummyGenericDeclaration.INSTANCE);
        keyType.setBounds(List.of(getObjectType()));
        primTypeFactory.putType(MetaMap.class.getTypeParameters()[0], keyType);
        var valueType = new TypeVariable(null, "词典值", "MapValue",
                DummyGenericDeclaration.INSTANCE);
        valueType.setBounds(List.of(getObjectType()));
        primTypeFactory.putType(MetaMap.class.getTypeParameters()[1], valueType);
        var pSetType = defContext.getGenericContext().getParameterizedType(StandardTypes.getSetType(), keyType);
        ClassType mapType = ClassTypeBuilder.newBuilder(name, code)
                .source(ClassSource.BUILTIN)
                .dependencies(List.of(pSetType))
                .typeParameters(keyType, valueType)
                .build();
        primTypeFactory.putType(MetaMap.class, mapType);
        FieldBuilder
                .newBuilder("键数组", "keyArray", mapType, defContext.getArrayType(keyType, ArrayKind.READ_WRITE))
                .access(Access.PRIVATE)
                .isChild(true)
                .nullType(getNullType())
                .build();
        FieldBuilder
                .newBuilder("值数组", "valueArray", mapType, defContext.getArrayType(valueType, ArrayKind.READ_WRITE))
                .access(Access.PRIVATE)
                .isChild(true)
                .nullType(getNullType())
                .build();
        createMapFlows(mapType, keyType, valueType);
        return mapType;
    }

    private void createMapFlows(ClassType mapType, Type keyType, Type valueType) {
        MethodBuilder.newBuilder(mapType, "词典", "Map", defContext.getFunctionTypeContext())
                .isConstructor(true)
                .isNative(true)
                .returnType(mapType)
                .build();

        var nullableValueType = defContext.getUnionType(Set.of(valueType, StandardTypes.getNullType()));

        MethodBuilder.newBuilder(mapType, "添加", "put", defContext.getFunctionTypeContext())
                .isNative(true)
                .returnType(nullableValueType)
                .parameters(new Parameter(null, "键", "key", keyType),
                        new Parameter(null, "值", "value", valueType))
                .build();

        MethodBuilder.newBuilder(mapType, "查询", "get", defContext.getFunctionTypeContext())
                .isNative(true)
                .returnType(nullableValueType)
                .parameters(new Parameter(null, "键", "key", keyType))
                .build();

        MethodBuilder.newBuilder(mapType, "删除", "remove", defContext.getFunctionTypeContext())
                .isNative(true)
                .returnType(nullableValueType)
                .parameters(new Parameter(null, "键", "key", keyType))
                .build();

        MethodBuilder.newBuilder(mapType, "计数", "size", defContext.getFunctionTypeContext())
                .isNative(true)
                .returnType(StandardTypes.getLongType())
                .build();

        MethodBuilder.newBuilder(mapType, "清空", "clear", defContext.getFunctionTypeContext())
                .isNative(true)
                .returnType(StandardTypes.getVoidType())
                .build();
        mapType.setStage(ResolutionStage.DEFINITION);
    }

    private void createThrowableFlows(ClassType throwableType) {
        MethodBuilder.newBuilder(throwableType, "Throwable", "Throwable", defContext.getFunctionTypeContext())
                .isConstructor(true)
                .isNative(true)
                .returnType(throwableType)
                .build();

        MethodBuilder.newBuilder(throwableType, "Throwable", "Throwable", defContext.getFunctionTypeContext())
                .isConstructor(true)
                .isNative(true)
                .returnType(throwableType)
                .parameters(new Parameter(null, "错误详情", "message", StandardTypes.getStringType()))
                .build();

        MethodBuilder.newBuilder(throwableType, "Throwable", "Throwable", defContext.getFunctionTypeContext())
                .isConstructor(true)
                .isNative(true)
                .returnType(throwableType)
                .parameters(new Parameter(null, "原因", "cause", throwableType))
                .build();

        MethodBuilder.newBuilder(throwableType, "Throwable", "Throwable", defContext.getFunctionTypeContext())
                .isConstructor(true)
                .isNative(true)
                .returnType(throwableType)
                .parameters(
                        new Parameter(null, "错误详情", "message", StandardTypes.getStringType()),
                        new Parameter(null, "原因", "cause", throwableType)
                )
                .build();

        MethodBuilder.newBuilder(throwableType, "获取详情", "getMessage", defContext.getFunctionTypeContext())
                .isNative(true)
                .returnType(defContext.getNullableType(StandardTypes.getStringType()))
                .build();
    }

    private void createExceptionFlows(ClassType exceptionType) {
        MethodBuilder.newBuilder(exceptionType, "Exception", "Exception", defContext.getFunctionTypeContext())
                .isConstructor(true)
                .isNative(true)
                .returnType(exceptionType)
                .build();

        MethodBuilder.newBuilder(exceptionType, "Exception", "Exception", defContext.getFunctionTypeContext())
                .isConstructor(true)
                .isNative(true)
                .returnType(exceptionType)
                .parameters(new Parameter(null, "错误详情", "message", StandardTypes.getStringType()))
                .build();

        MethodBuilder.newBuilder(exceptionType, "Exception", "Exception", defContext.getFunctionTypeContext())
                .isConstructor(true)
                .isNative(true)
                .returnType(exceptionType)
                .parameters(new Parameter(null, "原因", "cause", StandardTypes.getThrowableType()))
                .build();

        MethodBuilder.newBuilder(exceptionType, "Exception", "Exception", defContext.getFunctionTypeContext())
                .isConstructor(true)
                .isNative(true)
                .returnType(exceptionType)
                .parameters(
                        new Parameter(null, "错误详情", "message", StandardTypes.getStringType()),
                        new Parameter(null, "原因", "cause", StandardTypes.getThrowableType())
                )
                .build();
    }

    private void createRuntimeExceptionFlows(ClassType runtimeExceptionType) {
        MethodBuilder.newBuilder(runtimeExceptionType, "RuntimeException", "RuntimeException", defContext.getFunctionTypeContext())
                .isConstructor(true)
                .isNative(true)
                .returnType(runtimeExceptionType)
                .build();

        MethodBuilder.newBuilder(runtimeExceptionType, "RuntimeException", "RuntimeException", defContext.getFunctionTypeContext())
                .isConstructor(true)
                .isNative(true)
                .returnType(runtimeExceptionType)
                .parameters(new Parameter(null, "错误详情", "message", StandardTypes.getStringType()))
                .build();

        MethodBuilder.newBuilder(runtimeExceptionType, "RuntimeException", "RuntimeException", defContext.getFunctionTypeContext())
                .isConstructor(true)
                .isNative(true)
                .returnType(runtimeExceptionType)
                .parameters(new Parameter(null, "原因", "cause", StandardTypes.getThrowableType()))
                .build();

        MethodBuilder.newBuilder(runtimeExceptionType, "RuntimeException", "RuntimeException", defContext.getFunctionTypeContext())
                .isConstructor(true)
                .isNative(true)
                .returnType(runtimeExceptionType)
                .parameters(
                        new Parameter(null, "错误详情", "message", StandardTypes.getStringType()),
                        new Parameter(null, "原因", "cause", StandardTypes.getThrowableType())
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

        @Override
        public Type getType(java.lang.reflect.Type javaType) {
            return javaType2Type.getOrDefault(javaType, auxJavaType2Type.get(javaType));
        }

        public void addAuxType(java.lang.reflect.Type javaType, Type type) {
            auxJavaType2Type.put(javaType, type);
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
                case TypeVariable typeVariable -> new TypeVariableDef(
                        (java.lang.reflect.TypeVariable<?>) javaType, typeVariable
                );
                default -> new DirectDef<>(javaType, type, NATIVE_CLASS_MAP.get(javaType));
            };
            defMap.preAddDef(def);
            return def;
        }


    }

}
