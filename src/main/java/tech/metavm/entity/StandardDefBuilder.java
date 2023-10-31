package tech.metavm.entity;

import tech.metavm.flow.Flow;
import tech.metavm.flow.FlowBuilder;
import tech.metavm.flow.Parameter;
import tech.metavm.object.instance.*;
import tech.metavm.object.meta.*;
import tech.metavm.util.*;

import java.lang.reflect.Field;
import java.util.*;

import static tech.metavm.object.meta.TypeUtil.getParameterizedCode;
import static tech.metavm.object.meta.TypeUtil.getParameterizedName;
import static tech.metavm.util.ReflectUtils.*;

public class StandardDefBuilder {

    private ObjectTypeDef<Object> objectDef;

    private ValueDef<Enum<?>> enumDef;

    private FieldDef enumNameDef;

    private FieldDef enumOrdinalDef;

    private Map<Class<?>, PrimitiveType> primitiveTypeMap;

    private PrimitiveType longType;

    private PrimitiveType nullType;

    private PrimitiveType stringType;

    private PrimitiveType boolType;

    private PrimitiveType voidType;

    private ClassType iteratorType;

    private ClassType iteratorImplType;

    private ClassType collectionType;

    private ClassType setType;

    private ClassType throwableType;


    private final DefContext defContext;

//    private final FunctionTypeContext defContext.getFunctionTypeContext() = new FunctionTypeContext(null);

//    private final UnionTypeContext unionTypeContext = new UnionTypeContext(null);

//    private final ArrayTypeContext defContext.getArrayTypeContext() = new ArrayTypeContext(null);

    private final PrimTypeFactory primTypeFactory = new PrimTypeFactory();

//    private final GenericContext defContext.getGenericContext() = new GenericContext(functionTypeContext, unionTypeContext, defContext.getArrayTypeContext(), primTypeFactory);

    public StandardDefBuilder(DefContext defContext) {
        this.defContext = defContext;
    }

    public void initRootTypes() {
        ObjectType objectType = new ObjectType();

        objectDef = new ObjectTypeDef<>(
                Object.class,
                objectType
        );

        NothingType nothingType = new NothingType();
        var notingDef = new DirectDef<>(Nothing.class, nothingType);
        defContext.addDef(notingDef);

        primTypeFactory.addAuxType(Nothing.class, nothingType);
        primTypeFactory.addAuxType(Object.class, objectType);
        nullType = primTypeFactory.createPrimitive(PrimitiveKind.NULL);
        longType = primTypeFactory.createPrimitive(PrimitiveKind.LONG);
        var doubleType = primTypeFactory.createPrimitive(PrimitiveKind.DOUBLE);
        stringType = primTypeFactory.createPrimitive(PrimitiveKind.STRING);
        boolType = primTypeFactory.createPrimitive(PrimitiveKind.BOOLEAN);
        var timeType = primTypeFactory.createPrimitive(PrimitiveKind.TIME);
        var passwordType = primTypeFactory.createPrimitive(PrimitiveKind.PASSWORD);
        voidType = primTypeFactory.createPrimitive(PrimitiveKind.VOID);
        primitiveTypeMap = Map.ofEntries(
                Map.entry(Null.class, nullType),
                Map.entry(Long.class, longType),
                Map.entry(Double.class, doubleType),
                Map.entry(String.class, stringType),
                Map.entry(Boolean.class, boolType),
                Map.entry(Date.class, timeType),
                Map.entry(Password.class, passwordType),
                Map.entry(Void.class, voidType)
        );

        primitiveTypeMap.forEach((klass, primType) -> defContext.addDef(new PrimitiveDef<>(klass, primType)));

        var collectionTypeMap = new LinkedHashMap<Class<?>, ClassType>();
        collectionTypeMap.put(Iterator.class, iteratorType = createIteratorType());
        collectionTypeMap.put(Collection.class, collectionType = createCollectionType());
        collectionTypeMap.put(IteratorImpl.class, iteratorImplType = createIteratorImplType());
        collectionTypeMap.put(List.class, setType = createSetType());
        collectionTypeMap.put(Set.class, createListType());
        collectionTypeMap.put(Map.class, createMapType());

        defContext.addDef(objectDef);
        defContext.createCompositeTypes(Object.class, objectType);

        for (var entry : primitiveTypeMap.entrySet()) {
            var javaClass = entry.getKey();
            var primType = entry.getValue();
            if (!primType.isNull() && !primType.isVoid()) {
                defContext.createCompositeTypes(javaClass, primType);
                collectionTypeMap.forEach((collClass, collType) -> {
                    if (collClass != Map.class) {
                        defContext.getGenericContext().getParameterizedType(collType, primType);
                    }
                });
            }
        }

        ValueDef<Record> recordDef = createValueDef(
                Record.class,
                Record.class,
                ClassBuilder.newBuilder("记录", Record.class.getSimpleName())
                        .source(ClassSource.REFLECTION)
                        .category(TypeCategory.VALUE).build(),
                defContext
        );
        defContext.addDef(recordDef);

        EntityDef<Entity> entityDef = createEntityDef(
                Entity.class,
                Entity.class,
                ClassBuilder.newBuilder("实体", Entity.class.getSimpleName())
                        .source(ClassSource.REFLECTION)
                        .build(),
                defContext
        );

        defContext.addDef(entityDef);

        var enumTypeParam = new TypeVariable(null, "枚举类型", "Enum-E",
                DummyGenericDeclaration.INSTANCE);
        primTypeFactory.addAuxType(Enum.class.getTypeParameters()[0], enumTypeParam);
        var enumType = ClassBuilder.newBuilder("枚举", Enum.class.getSimpleName())
                .source(ClassSource.REFLECTION)
                .typeParameters(enumTypeParam)
                .build();
        primTypeFactory.addAuxType(Enum.class, enumType);

        var pEnumType = defContext.getGenericContext().getParameterizedType(enumType, enumTypeParam);
        enumTypeParam.setBounds(List.of(pEnumType));

        enumDef = createValueDef(
                Enum.class,// Enum is not a RuntimeGeneric, use the raw class
                new TypeReference<Enum<?>>() {
                }.getType(),
                enumType,
                defContext
        );

        enumNameDef = createFieldDef(
                ENUM_NAME_FIELD,
                createField(ENUM_NAME_FIELD, true, stringType, SQLType.VARCHAR64.getColumn(0), enumType),
                enumDef
        );

        enumOrdinalDef = createFieldDef(
                ENUM_ORDINAL_FIELD,
                createField(ENUM_ORDINAL_FIELD, false, longType, SQLType.INT64.getColumn(0), enumType),
                enumDef
        );

        enumType.setStage(ResolutionStage.GENERATED);
        defContext.getGenericContext().generateCode(enumType);

        var enumTypeParamDef = new TypeVariableDef(Enum.class.getTypeParameters()[0], enumTypeParam);
        var pEnumTypeDef = new DirectDef<>(
                ParameterizedTypeImpl.create(Enum.class, Enum.class.getTypeParameters()[0]),
                pEnumType
        );
        defContext.preAddDef(enumTypeParamDef);
        defContext.preAddDef(pEnumTypeDef);
        defContext.addDef(enumDef);
        defContext.afterDefInitialized(enumTypeParamDef);
        defContext.afterDefInitialized(pEnumTypeDef);

        defContext.addDef(new InstanceDef<>(Instance.class, objectType));
        defContext.addDef(new InstanceDef<>(ClassInstance.class, objectType));
        defContext.addDef(new InstanceDef<>(ArrayInstance.class, objectType));

        primTypeFactory.saveDefs(defContext);

        primTypeFactory.getMap().keySet().forEach(javaType ->
                defContext.afterDefInitialized(defContext.getDef(javaType))
        );

        throwableType = ClassBuilder.newBuilder("中断", Throwable.class.getSimpleName())
                .collectionName("Throwable")
                .source(ClassSource.REFLECTION).build();
        createThrowableFlows(throwableType);
        var throwableDef = createValueDef(
                Throwable.class,
                Throwable.class,
                throwableType,
                defContext
        );
        defContext.preAddDef(throwableDef);
        primTypeFactory.addAuxType(Throwable.class, throwableType);
        var javaMessageField = ReflectUtils.getField(Throwable.class, "detailMessage");
        /*
         Predefine composite types because the cause field depends on Throwable | Null

         Can't call createCompositeTypes here. Because it will make the Throwable type been
         initialized prematurely (without fields)
         */
        defContext.predefineCompositeTypes(Throwable.class, throwableType);
        createFieldDef(
                javaMessageField,
                createField(javaMessageField, true,
                        defContext.getNullableType(stringType),
                        SQLType.VARCHAR64.getColumn(0), throwableType),
                throwableDef
        );

        var javaCauseField = ReflectUtils.getField(Throwable.class, "cause");
        createFieldDef(
                javaCauseField,
                createField(javaCauseField, false,
                        defContext.getNullableType(throwableType),
                        SQLType.REFERENCE.getColumn(0), throwableType),
                throwableDef
        );
        defContext.afterDefInitialized(throwableDef);
        defContext.initCompositeTypes(Throwable.class);

        var exceptionType = ClassBuilder.newBuilder("异常", Exception.class.getSimpleName())
                .collectionName("Exception")
                .superType(throwableType)
                .source(ClassSource.REFLECTION).build();

        createExceptionFlows(exceptionType);
        defContext.addDef(createValueDef(Exception.class, Exception.class, exceptionType, defContext));

        var runtimeExceptionType = ClassBuilder.newBuilder("运行时异常", RuntimeException.class.getSimpleName())
                .collectionName("RuntimeException")
                .superType(exceptionType)
                .source(ClassSource.REFLECTION).build();
        createRuntimeExceptionFlows(runtimeExceptionType);
        defContext.addDef(createValueDef(RuntimeException.class, RuntimeException.class, runtimeExceptionType, defContext));
    }

    @SuppressWarnings("SameParameterValue")
    private <T extends Entity> EntityDef<T> createEntityDef(java.lang.reflect.Type javaType,
                                                            Class<T> javaClass,
                                                            ClassType type,
                                                            DefMap defMap) {
        return new EntityDef<>(
                javaClass,
                javaType,
                null,
                type,
                defMap
        );
    }

    @SuppressWarnings("SameParameterValue")
    private <T> ValueDef<T> createValueDef(java.lang.reflect.Type javaType,
                                           Class<T> javaClass,
                                           ClassType type,
                                           DefMap defMap) {
        return new ValueDef<>(
                javaClass,
                javaType,
                null,
                type,
                defMap
        );
    }

    private tech.metavm.object.meta.Field createField(Field javaField,
                                                      boolean asTitle,
                                                      Type type,
                                                      Column column,
                                                      ClassType declaringType) {
        return FieldBuilder.newBuilder(
                        getMetaFieldName(javaField),
                        javaField.getName(),
                        declaringType, type)
                .asTitle(asTitle)
                .column(column)
                .defaultValue(new NullInstance(getNullType()))
                .staticValue(new NullInstance(getNullType()))
                .build();
    }

    public PrimitiveType getNullType() {
        return primitiveTypeMap.get(Null.class);
    }

    public ObjectTypeDef<Object> getObjectDef() {
        return objectDef;
    }

    public ValueDef<Enum<?>> getEnumDef() {
        return enumDef;
    }

    private FieldDef createFieldDef(Field reflectField,
                                    tech.metavm.object.meta.Field field,
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

    @SuppressWarnings("unused")
    public ObjectType getObjectType() {
        return objectDef.getType();
    }

    @SuppressWarnings("unused")
    public Type getLongDef() {
        return primitiveTypeMap.get(Long.class);
    }

    public ClassType getEnumType() {
        return enumDef.getType();
    }

    public tech.metavm.object.meta.Field getEnumNameField() {
        return enumNameDef.getField();
    }

    public tech.metavm.object.meta.Field getEnumOrdinalField() {
        return enumOrdinalDef.getField();
    }

    public ClassType createIteratorType() {
        String name = getParameterizedName("迭代器");
        String code = getParameterizedCode("Iterator");
        var elementType = new TypeVariable(null, "迭代器元素", "IteratorElement",
                DummyGenericDeclaration.INSTANCE);
        elementType.setBounds(List.of(getObjectType()));
        primTypeFactory.putType(Iterator.class.getTypeParameters()[0], elementType);
        ClassType iteratorType = ClassBuilder.newBuilder(name, code)
                .typeParameters(elementType)
                .collectionName("Iterator")
                .category(TypeCategory.INTERFACE).build();
        primTypeFactory.putType(Iterator.class, iteratorType);
        createIteratorFlows(iteratorType, elementType);
        return iteratorType;
    }

    private void createIteratorFlows(ClassType iteratorType, Type elementType) {
        boolean isAbstract = iteratorType.isInterface();
        boolean isNative = !iteratorType.isInterface();
        FlowBuilder.newBuilder(iteratorType, "是否存在次项", "hasNext", defContext.getFunctionTypeContext())
                .nullType(getNullType())
                .isNative(isNative)
                .isAbstract(isAbstract)
                .returnType(boolType)
                .build();

        FlowBuilder.newBuilder(iteratorType, "获取次项", "next", defContext.getFunctionTypeContext())
                .nullType(getNullType())
                .isAbstract(isAbstract)
                .isNative(isNative)
                .returnType(elementType)
                .build();

        iteratorType.setStage(ResolutionStage.GENERATED);
    }


    public ClassType createCollectionType() {
        String name = getParameterizedName("Collection");
        String code = getParameterizedCode("Collection");
        var elementType = new TypeVariable(null, "Collection元素", "CollectionElement",
                DummyGenericDeclaration.INSTANCE);
        elementType.setBounds(List.of(getObjectType()));
        primTypeFactory.putType(Collection.class.getTypeParameters()[0], elementType);
        ClassType collectionType = ClassBuilder.newBuilder(name, code)
                .typeParameters(elementType)
                .collectionName("Collection")
                .category(TypeCategory.INTERFACE)
                .build();
        primTypeFactory.putType(Collection.class, collectionType);
        var pIteratorType = defContext.getGenericContext().getParameterizedType(iteratorType, elementType);
        createCollectionFlows(collectionType, pIteratorType, elementType);
        return collectionType;
    }

    private void createCollectionFlows(ClassType collectionType, ClassType iteratorType,
                                       TypeVariable elementType) {
        FlowBuilder.newBuilder(collectionType, "获取迭代器", "iterator", defContext.getFunctionTypeContext())
                .nullType(getNullType())
                .isNative(true)
                .returnType(iteratorType)
                .build();

        FlowBuilder.newBuilder(collectionType, "计数", "size", defContext.getFunctionTypeContext())
                .nullType(getNullType())
                .isNative(true)
                .returnType(longType)
                .build();

        FlowBuilder.newBuilder(collectionType, "是否为空", "isEmpty", defContext.getFunctionTypeContext())
                .nullType(getNullType())
                .isNative(true)
                .returnType(boolType)
                .build();

        FlowBuilder.newBuilder(collectionType, "是否包含", "contains", defContext.getFunctionTypeContext())
                .nullType(getNullType())
                .isNative(true)
                .returnType(boolType)
                .parameters(new Parameter(null, "元素", "element", elementType))
                .build();

        FlowBuilder.newBuilder(collectionType, "添加", "add", defContext.getFunctionTypeContext())
                .nullType(getNullType())
                .isNative(true)
                .returnType(boolType)
                .parameters(new Parameter(null, "元素", "element", elementType))
                .build();

        FlowBuilder.newBuilder(collectionType, "删除", "remove", defContext.getFunctionTypeContext())
                .nullType(getNullType())
                .isNative(true)
                .returnType(boolType)
                .parameters(new Parameter(null, "元素", "element", elementType))
                .build();

        FlowBuilder.newBuilder(collectionType, "清空", "clear", defContext.getFunctionTypeContext())
                .nullType(getNullType())
                .isNative(true)
                .returnType(voidType)
                .build();

        collectionType.setStage(ResolutionStage.GENERATED);
    }

    public ClassType createSetType() {
        String name = getParameterizedName("集合");
        String code = getParameterizedCode("Set");
        var elementType = new TypeVariable(null, "集合元素", "SetElement",
                DummyGenericDeclaration.INSTANCE);
        elementType.setBounds(List.of(getObjectType()));
        primTypeFactory.putType(Set.class.getTypeParameters()[0], elementType);
        var pCollectionType = defContext.getGenericContext().getParameterizedType(collectionType, elementType);
        var pIteratorImplType = defContext.getGenericContext().getParameterizedType(iteratorImplType, elementType);
        ClassType setType = ClassBuilder.newBuilder(name, code)
                .category(TypeCategory.INTERFACE)
                .interfaces(pCollectionType)
                .typeParameters(elementType)
                .dependencies(List.of(pIteratorImplType))
                .collectionName("Set")
                .build();
        var pSetType = defContext.getGenericContext().getParameterizedType(setType, elementType);
        primTypeFactory.putType(Set.class, setType);
        FieldBuilder.newBuilder("数组", "array", setType, defContext.getArrayTypeContext(ArrayKind.READ_WRITE).get(elementType))
                .nullType(getNullType())
                .access(Access.CLASS)
                .isChild(true).build();
        createSetFlows(setType, pSetType, pCollectionType);
        return setType;
    }

    private void createSetFlows(ClassType setType, ClassType pSetType, ClassType collectionType) {
        FlowBuilder.newBuilder(setType, "初始化", "Set", defContext.getFunctionTypeContext())
                .nullType(getNullType())
                .isNative(true)
                .returnType(pSetType)
                .build();
        createOverridingFlows(setType, collectionType);
        setType.setStage(ResolutionStage.GENERATED);
        defContext.getGenericContext().generateCode(pSetType, setType);
    }

    public ClassType createListType() {
        String name = getParameterizedName("列表");
        String code = getParameterizedName("List");
        var elementType = new TypeVariable(null, "列表元素", "ListElement",
                DummyGenericDeclaration.INSTANCE);
        elementType.setBounds(List.of(getObjectType()));
        primTypeFactory.putType(List.class.getTypeParameters()[0], elementType);
        var pCollectionType = defContext.getGenericContext().getParameterizedType(collectionType, elementType);
        var pIteratorImplType = defContext.getGenericContext().getParameterizedType(iteratorImplType, elementType);
        ClassType listType = ClassBuilder.newBuilder(name, code)
                .category(TypeCategory.INTERFACE)
                .interfaces(pCollectionType)
                .typeParameters(elementType)
                .dependencies(List.of(pIteratorImplType))
                .collectionName("List")
                .build();
        primTypeFactory.putType(List.class, listType);
        var pListType = defContext.getGenericContext().getParameterizedType(listType, elementType);
        FieldBuilder.newBuilder("数组", "array", listType, defContext.getArrayTypeContext(ArrayKind.READ_WRITE).get(elementType))
                .nullType(getNullType())
                .access(Access.CLASS)
                .isChild(true).build();
        createListFlows(listType, pListType, pCollectionType);
        return listType;
    }

    private void createListFlows(ClassType listType, ClassType pListType, ClassType collectionType) {
        FlowBuilder.newBuilder(listType, "List", "List", defContext.getFunctionTypeContext())
                .nullType(getNullType())
                .isNative(true)
                .returnType(pListType)
                .build();
        createOverridingFlows(listType, collectionType);
        listType.setStage(ResolutionStage.GENERATED);
        defContext.getGenericContext().generateCode(pListType, listType);
    }

    public ClassType createIteratorImplType() {
        String name = getParameterizedName("迭代器实现");
        String code = getParameterizedCode("IteratorImpl");
        var elementType = new TypeVariable(null, "迭代器实现元素", "IteratorImplElement",
                DummyGenericDeclaration.INSTANCE);
        elementType.setBounds(List.of(getObjectType()));
        primTypeFactory.putType(IteratorImpl.class.getTypeParameters()[0], elementType);
        var pIteratorType = defContext.getGenericContext().getParameterizedType(iteratorType, elementType);
        ClassType iteratorImplType = ClassBuilder.newBuilder(name, code)
                .interfaces(List.of(pIteratorType))
                .typeParameters(elementType)
                .collectionName("IteratorImpl")
                .build();
        primTypeFactory.putType(IteratorImpl.class, iteratorImplType);
        var pCollectionType = defContext.getGenericContext().getParameterizedType(collectionType, elementType);
        ClassType pIteratorImplType = defContext.getGenericContext().getParameterizedType(iteratorImplType, elementType);
        FlowBuilder.newBuilder(iteratorImplType, "IteratorImpl", "IteratorImpl", defContext.getFunctionTypeContext())
                .nullType(getNullType())
                .isConstructor(true)
                .isNative(true)
                .returnType(pIteratorImplType)
                .parameters(new Parameter(null, "集合", "collection", pCollectionType))
                .build();
        createOverridingFlows(iteratorImplType, pIteratorType);
        iteratorImplType.setStage(ResolutionStage.GENERATED);
        defContext.getGenericContext().generateCode(pIteratorImplType, iteratorImplType);
        return iteratorImplType;
    }

    private void createOverridingFlows(ClassType declaringType, ClassType baseType) {
        for (Flow flow : baseType.getFlows()) {
            FlowBuilder.newBuilder(declaringType, flow.getName(), flow.getCode(), defContext.getFunctionTypeContext())
                    .nullType(getNullType())
                    .isNative(true)
                    .overriden(List.of(flow))
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
        primTypeFactory.putType(Map.class.getTypeParameters()[0], keyType);
        var valueType = new TypeVariable(null, "词典值", "MapValue",
                DummyGenericDeclaration.INSTANCE);
        valueType.setBounds(List.of(getObjectType()));
        primTypeFactory.putType(Map.class.getTypeParameters()[1], valueType);
        var pSetType = defContext.getGenericContext().getParameterizedType(setType, keyType);
        ClassType mapType = ClassBuilder.newBuilder(name, code)
                .category(TypeCategory.INTERFACE)
                .collectionName("Map")
                .dependencies(List.of(pSetType))
                .typeParameters(keyType, valueType)
                .build();
        primTypeFactory.putType(Map.class, mapType);
        var pMapType = defContext.getGenericContext().getParameterizedType(mapType, keyType, valueType);
        FieldBuilder
                .newBuilder("键数组", "keyArray", mapType, defContext.getArrayTypeContext(ArrayKind.READ_WRITE).get(keyType))
                .access(Access.CLASS)
                .nullType(getNullType())
                .build();
        FieldBuilder
                .newBuilder("值数组", "valueArray", mapType, defContext.getArrayTypeContext(ArrayKind.READ_WRITE).get(valueType))
                .access(Access.CLASS)
                .nullType(getNullType())
                .build();
        createMapFlows(mapType, pMapType, keyType, valueType);
        return mapType;
    }

    private void createMapFlows(ClassType mapType, ClassType pMapType, Type keyType, Type valueType) {
        FlowBuilder.newBuilder(mapType, "Map", "Map", defContext.getFunctionTypeContext())
                .nullType(getNullType())
                .isConstructor(true)
                .isNative(true)
                .returnType(pMapType)
                .build();

        var nullableValueType = defContext.getUnionTypeContext().get(List.of(valueType, nullType));

        FlowBuilder.newBuilder(mapType, "添加", "put", defContext.getFunctionTypeContext())
                .nullType(getNullType())
                .isNative(true)
                .returnType(nullableValueType)
                .parameters(new Parameter(null, "键", "key", keyType),
                        new Parameter(null, "值", "value", valueType))
                .build();

        FlowBuilder.newBuilder(mapType, "查询", "get", defContext.getFunctionTypeContext())
                .nullType(getNullType())
                .isNative(true)
                .returnType(nullableValueType)
                .parameters(new Parameter(null, "键", "key", keyType))
                .build();

        FlowBuilder.newBuilder(mapType, "删除", "remove", defContext.getFunctionTypeContext())
                .nullType(getNullType())
                .isNative(true)
                .returnType(nullableValueType)
                .parameters(new Parameter(null, "键", "key", keyType))
                .build();

        FlowBuilder.newBuilder(mapType, "计数", "size", defContext.getFunctionTypeContext())
                .nullType(getNullType())
                .isNative(true)
                .returnType(longType)
                .build();

        FlowBuilder.newBuilder(mapType, "清空", "clear", defContext.getFunctionTypeContext())
                .isNative(true)
                .returnType(voidType)
                .build();
        mapType.setStage(ResolutionStage.GENERATED);
        defContext.getGenericContext().generateCode(pMapType, mapType);
    }

    private void createThrowableFlows(ClassType throwableType) {
        FlowBuilder.newBuilder(throwableType, "Throwable", "Throwable", defContext.getFunctionTypeContext())
                .nullType(nullType)
                .isConstructor(true)
                .isNative(true)
                .returnType(throwableType)
                .build();

        FlowBuilder.newBuilder(throwableType, "Throwable", "Throwable", defContext.getFunctionTypeContext())
                .nullType(nullType)
                .isConstructor(true)
                .isNative(true)
                .returnType(throwableType)
                .parameters(new Parameter(null, "错误详情", "message", stringType))
                .build();

        FlowBuilder.newBuilder(throwableType, "Throwable", "Throwable", defContext.getFunctionTypeContext())
                .nullType(nullType)
                .isConstructor(true)
                .isNative(true)
                .returnType(throwableType)
                .parameters(new Parameter(null, "原因", "cause", throwableType))
                .build();

        FlowBuilder.newBuilder(throwableType, "Throwable", "Throwable", defContext.getFunctionTypeContext())
                .nullType(nullType)
                .isConstructor(true)
                .isNative(true)
                .returnType(throwableType)
                .parameters(
                        new Parameter(null, "错误详情", "message", stringType),
                        new Parameter(null, "原因", "cause", throwableType)
                )
                .build();

        FlowBuilder.newBuilder(throwableType, "获取详情", "getMessage", defContext.getFunctionTypeContext())
                .nullType(nullType)
                .isNative(true)
                .returnType(defContext.getNullableType(stringType))
                .build();
    }

    private void createExceptionFlows(ClassType exceptionType) {
        FlowBuilder.newBuilder(exceptionType, "Exception", "Exception", defContext.getFunctionTypeContext())
                .nullType(nullType)
                .isConstructor(true)
                .isNative(true)
                .returnType(exceptionType)
                .build();

        FlowBuilder.newBuilder(exceptionType, "Exception", "Exception", defContext.getFunctionTypeContext())
                .nullType(nullType)
                .isConstructor(true)
                .isNative(true)
                .returnType(exceptionType)
                .parameters(new Parameter(null, "错误详情", "message", stringType))
                .build();

        FlowBuilder.newBuilder(exceptionType, "Exception", "Exception", defContext.getFunctionTypeContext())
                .nullType(nullType)
                .isConstructor(true)
                .isNative(true)
                .returnType(exceptionType)
                .parameters(new Parameter(null, "原因", "cause", throwableType))
                .build();

        FlowBuilder.newBuilder(exceptionType, "Exception", "Exception", defContext.getFunctionTypeContext())
                .nullType(nullType)
                .isConstructor(true)
                .isNative(true)
                .returnType(exceptionType)
                .parameters(
                        new Parameter(null, "错误详情", "message", stringType),
                        new Parameter(null, "原因", "cause", throwableType)
                )
                .build();
    }

    private void createRuntimeExceptionFlows(ClassType runtimeExceptionType) {
        FlowBuilder.newBuilder(runtimeExceptionType, "RuntimeException", "RuntimeException", defContext.getFunctionTypeContext())
                .nullType(nullType)
                .isConstructor(true)
                .isNative(true)
                .returnType(runtimeExceptionType)
                .build();

        FlowBuilder.newBuilder(runtimeExceptionType, "RuntimeException", "RuntimeException", defContext.getFunctionTypeContext())
                .nullType(nullType)
                .isConstructor(true)
                .isNative(true)
                .returnType(runtimeExceptionType)
                .parameters(new Parameter(null, "错误详情", "message", stringType))
                .build();

        FlowBuilder.newBuilder(runtimeExceptionType, "RuntimeException", "RuntimeException", defContext.getFunctionTypeContext())
                .nullType(nullType)
                .isConstructor(true)
                .isNative(true)
                .returnType(runtimeExceptionType)
                .parameters(new Parameter(null, "原因", "cause", throwableType))
                .build();

        FlowBuilder.newBuilder(runtimeExceptionType, "RuntimeException", "RuntimeException", defContext.getFunctionTypeContext())
                .nullType(nullType)
                .isConstructor(true)
                .isNative(true)
                .returnType(runtimeExceptionType)
                .parameters(
                        new Parameter(null, "错误详情", "message", stringType),
                        new Parameter(null, "原因", "cause", throwableType)
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
                default -> new DirectDef<>(javaType, type);
            };
            defMap.preAddDef(def);
            return def;
        }


    }

}
