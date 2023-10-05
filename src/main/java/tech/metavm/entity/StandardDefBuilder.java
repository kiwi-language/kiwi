package tech.metavm.entity;

import tech.metavm.flow.FlowBuilder;
import tech.metavm.flow.Parameter;
import tech.metavm.flow.Flow;
import tech.metavm.object.instance.ArrayInstance;
import tech.metavm.object.instance.ClassInstance;
import tech.metavm.object.instance.Instance;
import tech.metavm.object.instance.NullInstance;
import tech.metavm.object.meta.*;
import tech.metavm.object.meta.generic.GenericContext;
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

    private final PrimTypeFactory primTypeFactory = new PrimTypeFactory();

    private final GenericContext genericContext = new GenericContext(null, primTypeFactory);


    public StandardDefBuilder() {
    }

    public StandardDefBuilder(DefMap defMa) {
        initRootTypes(defMa);
    }

    public void initRootTypes(DefMap defMap) {
        TypeFactory typeFactory = new DefaultTypeFactory(defMap::getType);

        ObjectType objectType = new ObjectType();

        objectDef = new ObjectTypeDef<>(
                Object.class,
                objectType
        );
        defMap.addDef(objectDef);

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

        var collectionTypeMap = new LinkedHashMap<Class<?>, ClassType>();
        collectionTypeMap.put(Iterator.class, iteratorType = createIteratorType());
        collectionTypeMap.put(Collection.class, collectionType = createCollectionType());
        collectionTypeMap.put(IteratorImpl.class, iteratorImplType = createIteratorImplType());
        collectionTypeMap.put(List.class, setType = createSetType());
        collectionTypeMap.put(Set.class, createListType());
        collectionTypeMap.put(Map.class, createMapType());

        for (var entry : primitiveTypeMap.entrySet()) {
            var primType = entry.getValue();
            if (!primType.isNull() && !primType.isVoid()) {
                TypeUtil.fillCompositeTypes(primType, primTypeFactory);
                collectionTypeMap.forEach((collClass, collType) -> {
                    if (collClass != Map.class) {
                        genericContext.getParameterizedType(collType, primType);
                    }
                });
            }
        }

        primitiveTypeMap.forEach((klass, primType) -> defMap.addDef(new PrimitiveDef<>(klass, primType)));


        ValueDef<Record> recordDef = createValueDef(
                Record.class,
                Record.class,
                ClassBuilder.newBuilder("记录", Record.class.getSimpleName())
                        .source(ClassSource.REFLECTION)
                        .category(TypeCategory.VALUE).build(),
                defMap
        );
        defMap.addDef(recordDef);

        EntityDef<Entity> entityDef = createEntityDef(
                Entity.class,
                Entity.class,
                ClassBuilder.newBuilder("实体", Entity.class.getSimpleName())
                        .source(ClassSource.REFLECTION)
                        .build(),
                defMap
        );

        defMap.addDef(entityDef);

        objectType.setArrayType(typeFactory.createArrayType(objectType));
        defMap.addDef(
                CollectionDef.createHelper(
                        Table.class,
                        Table.class,
                        objectDef,
                        objectType.getArrayType()
                )
        );

        var enumTypeParam = new TypeVariable(null, "枚举类型", "Enum-E");
        primTypeFactory.addAuxType(Enum.class.getTypeParameters()[0], enumTypeParam);
        var enumType = ClassBuilder.newBuilder("枚举", Enum.class.getSimpleName())
                .source(ClassSource.REFLECTION)
                .typeParameters(enumTypeParam)
                .build();
        primTypeFactory.addAuxType(Enum.class, enumType);

        var pEnumType = genericContext.getParameterizedType(enumType, enumTypeParam);
        enumTypeParam.setBounds(List.of(pEnumType));

        enumDef = createValueDef(
                Enum.class,// Enum is not a RuntimeGeneric, use the raw class
                new TypeReference<Enum<?>>() {
                }.getType(),
                enumType,
                defMap
        );

        enumNameDef = createFieldDef(
                ENUM_NAME_FIELD,
                createField(ENUM_NAME_FIELD, true, stringType, enumType),
                enumDef
        );

        enumOrdinalDef = createFieldDef(
                ENUM_ORDINAL_FIELD,
                createField(ENUM_ORDINAL_FIELD, false, longType, enumType),
                enumDef
        );

        enumType.stage = ResolutionStage.GENERATED;
        genericContext.generateCode(enumType);

        var enumTypeParamDef = new TypeVariableDef(Enum.class.getTypeParameters()[0], enumTypeParam);
        var pEnumTypeDef = new DirectDef<>(
                ParameterizedTypeImpl.create(Enum.class, Enum.class.getTypeParameters()[0]),
                pEnumType
        );
        defMap.preAddDef(enumTypeParamDef);
        defMap.preAddDef(pEnumTypeDef);
        defMap.addDef(enumDef);
        defMap.afterDefInitialized(enumTypeParamDef);
        defMap.afterDefInitialized(pEnumTypeDef);

        defMap.addDef(new InstanceDef<>(Instance.class, objectType));
        defMap.addDef(new InstanceDef<>(ClassInstance.class, objectType));
        defMap.addDef(new InstanceDef<>(ArrayInstance.class, objectType));

        primTypeFactory.getMap().forEach((javaType, type) -> {
            if (!defMap.containsDef(javaType)) {
                switch (type) {
                    case ClassType classType -> defMap.preAddDef(new DirectDef<>(javaType, classType));
                    case TypeVariable typeVariable ->
                            defMap.preAddDef(new TypeVariableDef((java.lang.reflect.TypeVariable<?>) javaType, typeVariable));
                    default -> {
                    }
                }
            }
        });

        primTypeFactory.getMap().keySet().forEach(javaType ->
                defMap.afterDefInitialized(defMap.getDef(javaType))
        );

        throwableType = ClassBuilder.newBuilder("中断", Throwable.class.getSimpleName())
                .collectionName("Throwable")
                .source(ClassSource.REFLECTION).build();
        createThrowableFlows(throwableType);
        var throwableDef = createValueDef(
                Throwable.class,
                Throwable.class,
                throwableType,
                defMap
        );
        var javaMessageField = ReflectUtils.getField(Throwable.class, "detailMessage");
        TypeUtil.fillCompositeTypes(throwableType, primTypeFactory);
        createFieldDef(
                javaMessageField,
                createField(javaMessageField, true, stringType.getNullableType(), throwableType),
                throwableDef
        );

        var javaCauseField = ReflectUtils.getField(Throwable.class, "cause");
        createFieldDef(
                javaCauseField,
                createField(javaCauseField, false, throwableType.getNullableType(), throwableType),
                throwableDef
        );

        defMap.addDef(throwableDef);

        var exceptionType = ClassBuilder.newBuilder("异常", Exception.class.getSimpleName())
                .collectionName("Exception")
                .superType(throwableType)
                .source(ClassSource.REFLECTION).build();

        createExceptionFlows(exceptionType);
        defMap.addDef(createValueDef(Exception.class, Exception.class, exceptionType, defMap));

        var runtimeExceptionType = ClassBuilder.newBuilder("运行时异常", RuntimeException.class.getSimpleName())
                .collectionName("RuntimeException")
                .superType(exceptionType)
                .source(ClassSource.REFLECTION).build();
        createRuntimeExceptionFlows(runtimeExceptionType);
        defMap.addDef(createValueDef(RuntimeException.class, RuntimeException.class, runtimeExceptionType, defMap));
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
                                                      ClassType declaringType) {
        return FieldBuilder.newBuilder(
                        getMetaFieldName(javaField),
                        javaField.getName(),
                        declaringType, type)
                .asTitle(asTitle)
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
        var elementType = new TypeVariable(null, "迭代器元素", "IteratorElement");
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
        FlowBuilder.newBuilder(iteratorType, "是否存在次项", "hasNext")
                .nullType(getNullType())
                .isNative(isNative)
                .isAbstract(isAbstract)
                .outputType(boolType)
                .build();

        FlowBuilder.newBuilder(iteratorType, "获取次项", "next")
                .nullType(getNullType())
                .isAbstract(isAbstract)
                .isNative(isNative)
                .outputType(elementType)
                .build();

        iteratorType.stage = ResolutionStage.GENERATED;
    }


    public ClassType createCollectionType() {
        String name = getParameterizedName("Collection");
        String code = getParameterizedCode("Collection");
        var elementType = new TypeVariable(null, "Collection元素", "CollectionElement");
        elementType.setBounds(List.of(getObjectType()));
        primTypeFactory.putType(Collection.class.getTypeParameters()[0], elementType);
        ClassType collectionType = ClassBuilder.newBuilder(name, code)
                .typeParameters(elementType)
                .collectionName("Collection")
                .category(TypeCategory.INTERFACE)
                .build();
        primTypeFactory.putType(Collection.class, collectionType);
        var pIteratorType = genericContext.getParameterizedType(iteratorType, elementType);
        createCollectionFlows(collectionType, pIteratorType, elementType);
        return collectionType;
    }

    private void createCollectionFlows(ClassType collectionType, ClassType iteratorType,
                                       TypeVariable elementType) {
        FlowBuilder.newBuilder(collectionType, "获取迭代器", "iterator")
                .nullType(getNullType())
                .isNative(true)
                .outputType(iteratorType)
                .build();

        FlowBuilder.newBuilder(collectionType, "计数", "size")
                .nullType(getNullType())
                .isNative(true)
                .outputType(longType)
                .build();

        FlowBuilder.newBuilder(collectionType, "是否为空", "isEmpty")
                .nullType(getNullType())
                .isNative(true)
                .outputType(boolType)
                .build();

        FlowBuilder.newBuilder(collectionType, "是否包含", "contains")
                .nullType(getNullType())
                .isNative(true)
                .outputType(boolType)
                .parameters(new Parameter(null, "元素", "element", elementType))
                .build();

        FlowBuilder.newBuilder(collectionType, "添加", "add")
                .nullType(getNullType())
                .isNative(true)
                .outputType(boolType)
                .parameters(new Parameter(null, "元素", "element", elementType))
                .build();

        FlowBuilder.newBuilder(collectionType, "删除", "remove")
                .nullType(getNullType())
                .isNative(true)
                .outputType(boolType)
                .parameters(new Parameter(null, "元素", "element", elementType))
                .build();

        FlowBuilder.newBuilder(collectionType, "清空", "clear")
                .nullType(getNullType())
                .isNative(true)
                .outputType(voidType)
                .build();

        collectionType.stage = ResolutionStage.GENERATED;
    }

    public ClassType createSetType() {
        String name = getParameterizedName("集合");
        String code = getParameterizedCode("Set");
        var elementType = new TypeVariable(null, "集合元素", "SetElement");
        elementType.setBounds(List.of(getObjectType()));
        primTypeFactory.putType(Set.class.getTypeParameters()[0], elementType);
        var pCollectionType = genericContext.getParameterizedType(collectionType, elementType);
        var pIteratorImplType = genericContext.getParameterizedType(iteratorImplType, elementType);
        ClassType setType = ClassBuilder.newBuilder(name, code)
                .interfaces()
                .interfaces(pCollectionType)
                .typeParameters(elementType)
                .dependencies(List.of(pIteratorImplType))
                .collectionName("Set")
                .build();
        var pSetType = genericContext.getParameterizedType(setType, elementType);
        primTypeFactory.putType(Set.class, setType);
        FieldBuilder.newBuilder("数组", "array", setType, TypeUtil.getArrayType(elementType))
                .nullType(getNullType())
                .access(Access.CLASS)
                .isChild(true).build();
        createSetFlows(setType, pSetType, pCollectionType);
        return setType;
    }

    private void createSetFlows(ClassType setType, ClassType pSetType, ClassType collectionType) {
        FlowBuilder.newBuilder(setType, "初始化", "Set")
                .nullType(getNullType())
                .isNative(true)
                .outputType(pSetType)
                .parameters(List.of(new Parameter(null, "元素作为从对象", "elementAsChild", boolType)))
                .build();
        createOverridingFlows(setType, collectionType);
        setType.stage = ResolutionStage.GENERATED;
        genericContext.generateCode(pSetType, setType);
    }

    public ClassType createListType() {
        String name = getParameterizedName("列表");
        String code = getParameterizedName("List");
        var elementType = new TypeVariable(null, "列表元素", "ListElement");
        elementType.setBounds(List.of(getObjectType()));
        primTypeFactory.putType(List.class.getTypeParameters()[0], elementType);
        var pCollectionType = genericContext.getParameterizedType(collectionType, elementType);
        var pIteratorImplType = genericContext.getParameterizedType(iteratorImplType, elementType);
        ClassType listType = ClassBuilder.newBuilder(name, code)
                .interfaces(pCollectionType)
                .typeParameters(elementType)
                .dependencies(List.of(pIteratorImplType))
                .collectionName("List")
                .build();
        primTypeFactory.putType(List.class, listType);
        var pListType = genericContext.getParameterizedType(listType, elementType);
        FieldBuilder.newBuilder("数组", "array", listType, TypeUtil.getArrayType(elementType))
                .nullType(getNullType())
                .access(Access.CLASS)
                .isChild(true).build();
        createListFlows(listType, pListType, pCollectionType);
        return listType;
    }

    private void createListFlows(ClassType listType, ClassType pListType, ClassType collectionType) {
        FlowBuilder.newBuilder(listType, "List", "List")
                .nullType(getNullType())
                .isNative(true)
                .outputType(pListType)
                .parameters(List.of(new Parameter(null, "元素作为从对象", "elementAsChild", boolType)))
                .build();
        createOverridingFlows(listType, collectionType);
        listType.stage = ResolutionStage.GENERATED;
        genericContext.generateCode(pListType, listType);
    }

    public ClassType createIteratorImplType() {
        String name = getParameterizedName("迭代器实现");
        String code = getParameterizedCode("IteratorImpl");
        var elementType = new TypeVariable(null, "迭代器实现元素", "IteratorImplElement");
        elementType.setBounds(List.of(getObjectType()));
        primTypeFactory.putType(IteratorImpl.class.getTypeParameters()[0], elementType);
        var pIteratorType = genericContext.getParameterizedType(iteratorType, elementType);
        ClassType iteratorImplType = ClassBuilder.newBuilder(name, code)
                .interfaces(List.of(pIteratorType))
                .typeParameters(elementType)
                .collectionName("IteratorImpl")
                .build();
        primTypeFactory.putType(IteratorImpl.class, iteratorImplType);
        var pCollectionType = genericContext.getParameterizedType(collectionType, elementType);
        ClassType pIteratorImplType = genericContext.getParameterizedType(iteratorImplType, elementType);
        FlowBuilder.newBuilder(iteratorImplType, "IteratorImpl", "IteratorImpl")
                .nullType(getNullType())
                .isConstructor(true)
                .isNative(true)
                .outputType(pIteratorImplType)
                .parameters(new Parameter(null, "集合", "collection", pCollectionType))
                .build();
        createOverridingFlows(iteratorImplType, pIteratorType);
        iteratorImplType.stage = ResolutionStage.GENERATED;
        genericContext.generateCode(pIteratorImplType, iteratorImplType);
        return iteratorImplType;
    }

    private void createOverridingFlows(ClassType declaringType, ClassType baseType) {
        for (Flow flow : baseType.getFlows()) {
            FlowBuilder.newBuilder(declaringType, flow.getName(), flow.getCode())
                    .nullType(getNullType())
                    .isNative(true)
                    .overriden(flow)
                    .outputType(flow.getReturnType())
//                    .parameters(NncUtils.map(flow.getParameters(), Parameter::copy))
                    .build();
        }
    }

    public ClassType createMapType() {
        String name = getParameterizedName("词典");
        String code = getParameterizedName("Map");
        var keyType = new TypeVariable(null, "词典键", "MapKey");
        keyType.setBounds(List.of(getObjectType()));
        primTypeFactory.putType(Map.class.getTypeParameters()[0], keyType);
        var valueType = new TypeVariable(null, "词典值", "MapValue");
        valueType.setBounds(List.of(getObjectType()));
        primTypeFactory.putType(Map.class.getTypeParameters()[1], valueType);
        var pSetType = genericContext.getParameterizedType(setType, keyType);
        ClassType mapType = ClassBuilder.newBuilder(name, code)
                .collectionName("Map")
                .dependencies(List.of(pSetType))
                .typeParameters(keyType, valueType)
                .build();
        primTypeFactory.putType(Map.class, mapType);
        var pMapType = genericContext.getParameterizedType(mapType, keyType, valueType);
        FieldBuilder
                .newBuilder("键数组", "keyArray", mapType, TypeUtil.getArrayType(keyType, primTypeFactory))
                .access(Access.CLASS)
                .nullType(getNullType())
                .build();
        FieldBuilder
                .newBuilder("值数组", "valueArray", mapType, TypeUtil.getArrayType(valueType, primTypeFactory))
                .access(Access.CLASS)
                .nullType(getNullType())
                .build();
        createMapFlows(mapType, pMapType, keyType, valueType);
        return mapType;
    }

    private void createMapFlows(ClassType mapType, ClassType pMapType, Type keyType, Type valueType) {
        FlowBuilder.newBuilder(mapType, "Map", "Map")
                .nullType(getNullType())
                .isConstructor(true)
                .isNative(true)
                .outputType(pMapType)
                .parameters(
                        new Parameter(null, "键作为子对象", "keyAsChild", boolType),
                        new Parameter(null, "值作为子对象", "valueAsChild", boolType)
                )
                .build();

        FlowBuilder.newBuilder(mapType, "添加", "put")
                .nullType(getNullType())
                .isNative(true)
                .outputType(primTypeFactory.getNullableType(valueType))
                .parameters(new Parameter(null, "键", "key", keyType),
                        new Parameter(null, "值", "value", valueType))
                .build();

        FlowBuilder.newBuilder(mapType, "查询", "get")
                .nullType(getNullType())
                .isNative(true)
                .outputType(primTypeFactory.getNullableType(valueType))
                .parameters(new Parameter(null, "键", "key", keyType))
                .build();

        FlowBuilder.newBuilder(mapType, "删除", "remove")
                .nullType(getNullType())
                .isNative(true)
                .outputType(primTypeFactory.getNullableType(valueType))
                .parameters(new Parameter(null, "键", "key", keyType))
                .build();

        FlowBuilder.newBuilder(mapType, "计数", "size")
                .nullType(getNullType())
                .isNative(true)
                .outputType(longType)
                .build();

        FlowBuilder.newBuilder(mapType, "清空", "clear")
                .isNative(true)
                .outputType(voidType)
                .build();
        mapType.stage = ResolutionStage.GENERATED;
        genericContext.generateCode(pMapType, mapType);
    }

    private void createThrowableFlows(ClassType throwableType) {
        FlowBuilder.newBuilder(throwableType, "Throwable", "Throwable")
                .nullType(nullType)
                .isConstructor(true)
                .isNative(true)
                .outputType(throwableType)
                .build();

        FlowBuilder.newBuilder(throwableType, "Throwable", "Throwable")
                .nullType(nullType)
                .isConstructor(true)
                .isNative(true)
                .outputType(throwableType)
                .parameters(new Parameter(null, "错误详情", "message", stringType))
                .build();

        FlowBuilder.newBuilder(throwableType, "Throwable", "Throwable")
                .nullType(nullType)
                .isConstructor(true)
                .isNative(true)
                .outputType(throwableType)
                .parameters(new Parameter(null, "原因", "cause", throwableType))
                .build();

        FlowBuilder.newBuilder(throwableType, "Throwable", "Throwable")
                .nullType(nullType)
                .isConstructor(true)
                .isNative(true)
                .outputType(throwableType)
                .parameters(
                        new Parameter(null, "错误详情", "message", stringType),
                        new Parameter(null, "原因", "cause", throwableType)
                )
                .build();

        FlowBuilder.newBuilder(throwableType, "获取详情", "getMessage")
                .nullType(nullType)
                .isNative(true)
                .outputType(stringType.getNullableType())
                .build();
    }

    private void createExceptionFlows(ClassType exceptionType) {
        FlowBuilder.newBuilder(exceptionType, "Exception", "Exception")
                .nullType(nullType)
                .isConstructor(true)
                .isNative(true)
                .outputType(exceptionType)
                .build();

        FlowBuilder.newBuilder(exceptionType, "Exception", "Exception")
                .nullType(nullType)
                .isConstructor(true)
                .isNative(true)
                .outputType(exceptionType)
                .parameters(new Parameter(null, "错误详情", "message", stringType))
                .build();

        FlowBuilder.newBuilder(exceptionType, "Exception", "Exception")
                .nullType(nullType)
                .isConstructor(true)
                .isNative(true)
                .outputType(exceptionType)
                .parameters(new Parameter(null, "原因", "cause", throwableType))
                .build();

        FlowBuilder.newBuilder(exceptionType, "Exception", "Exception")
                .nullType(nullType)
                .isConstructor(true)
                .isNative(true)
                .outputType(exceptionType)
                .parameters(
                        new Parameter(null, "错误详情", "message", stringType),
                        new Parameter(null, "原因", "cause", throwableType)
                )
                .build();
    }

    private void createRuntimeExceptionFlows(ClassType runtimeExceptionType) {
        FlowBuilder.newBuilder(runtimeExceptionType, "RuntimeException", "RuntimeException")
                .nullType(nullType)
                .isConstructor(true)
                .isNative(true)
                .outputType(runtimeExceptionType)
                .build();

        FlowBuilder.newBuilder(runtimeExceptionType, "RuntimeException", "RuntimeException")
                .nullType(nullType)
                .isConstructor(true)
                .isNative(true)
                .outputType(runtimeExceptionType)
                .parameters(new Parameter(null, "错误详情", "message", stringType))
                .build();

        FlowBuilder.newBuilder(runtimeExceptionType, "RuntimeException", "RuntimeException")
                .nullType(nullType)
                .isConstructor(true)
                .isNative(true)
                .outputType(runtimeExceptionType)
                .parameters(new Parameter(null, "原因", "cause", throwableType))
                .build();

        FlowBuilder.newBuilder(runtimeExceptionType, "RuntimeException", "RuntimeException")
                .nullType(nullType)
                .isConstructor(true)
                .isNative(true)
                .outputType(runtimeExceptionType)
                .parameters(
                        new Parameter(null, "错误详情", "message", stringType),
                        new Parameter(null, "原因", "cause", throwableType)
                )
                .build();
    }

    private static class PrimTypeFactory extends TypeFactory {

        private final Map<java.lang.reflect.Type, Type> javaType2Type = new IdentityHashMap<>();
        private final Map<Type, java.lang.reflect.Type> type2JavaType = new IdentityHashMap<>();

        private final Map<java.lang.reflect.Type, Type> auxJavaType2Type = new IdentityHashMap<>();
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

    }

}
