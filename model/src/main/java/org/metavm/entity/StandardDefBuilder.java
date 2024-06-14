package org.metavm.entity;

import org.metavm.entity.natives.*;
import org.metavm.flow.FunctionBuilder;
import org.metavm.flow.Method;
import org.metavm.flow.MethodBuilder;
import org.metavm.flow.Parameter;
import org.metavm.object.instance.ColumnKind;
import org.metavm.object.instance.core.NullInstance;
import org.metavm.object.type.*;
import org.metavm.util.*;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static org.metavm.object.type.Types.getParameterizedCode;
import static org.metavm.object.type.Types.getParameterizedName;
import static org.metavm.util.ReflectionUtils.ENUM_NAME_FIELD;
import static org.metavm.util.ReflectionUtils.ENUM_ORDINAL_FIELD;

public class StandardDefBuilder {

    private ValueDef<Enum<?>> enumDef;

    private FieldDef enumNameDef;

    private FieldDef enumOrdinalDef;

    private final DefContext defContext;

    private final PrimTypeFactory primTypeFactory = new PrimTypeFactory();

    private static final Map<java.lang.reflect.Type, Class<?>> NATIVE_CLASS_MAP = Map.ofEntries(
            Map.entry(MetaSet.class, SetNative.class),
            Map.entry(ChildMetaList.class, ListNative.class),
            Map.entry(ReadWriteMetaList.class, ListNative.class),
            Map.entry(ValueMetaList.class, ListNative.class),
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
        initBuiltinFlows();

        StandardTypes.setConsumerKlass(createConsumerKlass());
        StandardTypes.setPredicateKlass(createPredicateKlass());
        StandardTypes.setIteratorKlass(createIteratorKlass());
        StandardTypes.setIterableKlass(createIterableKlass());
        StandardTypes.setCollectionKlass(createCollectionType());
        StandardTypes.setIteratorImplKlass(createIteratorImplKlass());
        StandardTypes.setSetKlass(createSetKlass());
        StandardTypes.setListKlass(createListKlass());
        StandardTypes.setReadWriteListKlass(createReadWriteListKlass());
        StandardTypes.setChildListKlass(createChildListKlass());
        StandardTypes.setValueListKlass(createValueListKlass());
        StandardTypes.setMapKlass(createMapKlass());

        ValueDef<Record> recordDef = createValueDef(
                Record.class,
                Record.class,
                StandardTypes.setRecordKlass(ClassTypeBuilder.newBuilder("Record", Record.class.getSimpleName())
                        .source(ClassSource.BUILTIN)
                        .kind(ClassKind.VALUE).build()),
                defContext
        );
        defContext.addDef(recordDef);

        StandardTypes.setEntityKlass(ClassTypeBuilder.newBuilder("Entity", Entity.class.getSimpleName())
                .source(ClassSource.BUILTIN)
                .build());
        EntityDef<Entity> entityDef = createEntityDef(
                Entity.class,
                Entity.class,
                StandardTypes.getEntityKlass(),
                defContext
        );

        defContext.addDef(entityDef);

        var enumTypeParam = new TypeVariable(null, "EnumType", "EnumType",
                DummyGenericDeclaration.INSTANCE);
        StandardTypes.setEnumKlass(ClassTypeBuilder.newBuilder("Enum", Enum.class.getSimpleName())
                .source(ClassSource.BUILTIN)
                .typeParameters(enumTypeParam)
                .build());

        enumTypeParam.setBounds(List.of(StandardTypes.getEnumKlass().getType()));

        enumDef = createValueDef(
                Enum.class,// Enum is not a RuntimeGeneric, use the raw class
                new TypeReference<Enum<?>>() {
                }.getType(),
                StandardTypes.getEnumKlass(),
                defContext
        );

        enumNameDef = createFieldDef(
                ENUM_NAME_FIELD,
                createField(ENUM_NAME_FIELD, true, StandardTypes.getStringType(), Access.PUBLIC,
                        ColumnKind.STRING.getColumn(0), StandardTypes.getEnumKlass()),
                enumDef
        );

        enumOrdinalDef = createFieldDef(
                ENUM_ORDINAL_FIELD,
                createField(ENUM_ORDINAL_FIELD, false, StandardTypes.getLongType(), Access.PRIVATE,
                        ColumnKind.INT.getColumn(0), StandardTypes.getEnumKlass()),
                enumDef
        );
        StandardTypes.getEnumKlass().setTitleField(enumNameDef.getField());
        StandardTypes.getEnumKlass().setStage(ResolutionStage.DEFINITION);

        var enumTypeParamDef = new TypeVariableDef(Enum.class.getTypeParameters()[0], enumTypeParam);
        defContext.preAddDef(enumTypeParamDef);
        defContext.addDef(enumDef);
        defContext.afterDefInitialized(enumTypeParamDef);

        primTypeFactory.saveDefs(defContext);

        primTypeFactory.getMap().keySet().forEach(javaType ->
                defContext.afterDefInitialized(defContext.getDef(javaType))
        );

        StandardTypes.setThrowableKlass(ClassTypeBuilder.newBuilder("Predicate", Throwable.class.getSimpleName())
                .source(ClassSource.BUILTIN).build());
        createThrowableFlows(StandardTypes.getThrowableKlass());
        var throwableDef = createValueDef(
                Throwable.class,
                Throwable.class,
                StandardTypes.getThrowableKlass(),
                defContext
        );
        defContext.preAddDef(throwableDef);
        var javaMessageField = ReflectionUtils.getField(Throwable.class, "detailMessage");
        /*
         Predefine composite types because the 'cause' field depends on Throwable | Null
         Do not call createCompositeTypes, it will initialize the throwable type without fields!
         */
        createFieldDef(
                javaMessageField,
                createField(javaMessageField, true,
                        StandardTypes.getNullableType(StandardTypes.getStringType()), Access.PUBLIC,
                        ColumnKind.STRING.getColumn(0), StandardTypes.getThrowableKlass()),
                throwableDef
        );

        var javaCauseField = ReflectionUtils.getField(Throwable.class, "cause");
        createFieldDef(
                javaCauseField,
                createField(javaCauseField, false,
                        StandardTypes.getNullableType(StandardTypes.getThrowableKlass().getType()), Access.PUBLIC,
                        ColumnKind.REFERENCE.getColumn(0), StandardTypes.getThrowableKlass()),
                throwableDef
        );
        defContext.afterDefInitialized(throwableDef);
        StandardTypes.setExceptionKlass(ClassTypeBuilder.newBuilder("Exception", Exception.class.getSimpleName())
                .superClass(StandardTypes.getThrowableKlass().getType())
                .source(ClassSource.BUILTIN).build());

        createExceptionFlows(StandardTypes.getExceptionKlass());
//        defContext.addDef(createValueDef(Exception.class, Exception.class, exceptionType, defContext));
        defContext.addDef(new DirectDef<>(Exception.class, StandardTypes.getExceptionKlass(), ExceptionNative.class));

        StandardTypes.setRuntimeExceptionKlass(ClassTypeBuilder.newBuilder("RuntimeException", RuntimeException.class.getSimpleName())
                .superClass(StandardTypes.getExceptionKlass().getType())
                .source(ClassSource.BUILTIN).build());
        createRuntimeExceptionFlows(StandardTypes.getRuntimeExceptionKlass());
        defContext.addDef(new DirectDef<>(
                RuntimeException.class, StandardTypes.getRuntimeExceptionKlass(), RuntimeExceptionNative.class));

        StandardTypes.setIllegalArgumentExceptionKlass(ClassTypeBuilder.newBuilder("IllegalArgumentException", IllegalArgumentException.class.getSimpleName())
                .superClass(StandardTypes.getRuntimeExceptionKlass().getType())
                .source(ClassSource.BUILTIN).build());
        createIllegalArgumentExceptionFlows(StandardTypes.getIllegalArgumentExceptionKlass());
        defContext.addDef(new DirectDef<>(
                IllegalArgumentException.class, StandardTypes.getIllegalArgumentExceptionKlass(), IllegalArgumentExceptionNative.class));

        StandardTypes.setIllegalStateExceptionKlass(ClassTypeBuilder.newBuilder("IllegalStateException", IllegalStateException.class.getSimpleName())
                .superClass(StandardTypes.getRuntimeExceptionKlass().getType())
                .source(ClassSource.BUILTIN).build());
        createIllegalStateExceptionFlows(StandardTypes.getIllegalStateExceptionKlass());
        defContext.addDef(new DirectDef<>(
                IllegalStateException.class, StandardTypes.getIllegalStateExceptionKlass(), IllegalStateExceptionNative.class));

        StandardTypes.setNullPointerExceptionKlass(ClassTypeBuilder.newBuilder("NullPointerException", NullPointerException.class.getSimpleName())
                .superClass(StandardTypes.getRuntimeExceptionKlass().getType())
                .source(ClassSource.BUILTIN).build());
        createNullPointerExceptionFlows(StandardTypes.getNullPointerExceptionKlass());
        defContext.addDef(new DirectDef<>(
                NullPointerException.class, StandardTypes.getNullPointerExceptionKlass(), NullPointerException.class));
    }

    private Klass createConsumerKlass() {
        var elementType = new TypeVariable(null, "Element", "T",
                DummyGenericDeclaration.INSTANCE);
        elementType.setBounds(List.of(new AnyType()));
        primTypeFactory.putType(Consumer.class.getTypeParameters()[0], elementType);
        var consumerType = ClassTypeBuilder.newBuilder("Consumer", "Consumer")
                .typeParameters(elementType)
                .source(ClassSource.BUILTIN)
                .kind(ClassKind.INTERFACE)
                .build();
        primTypeFactory.putType(Consumer.class, consumerType);
        MethodBuilder.newBuilder(consumerType, "accept", "accept")
                .returnType(StandardTypes.getVoidType())
                .parameters(new Parameter(null, "element", "element", elementType.getType()))
                .build();
        return consumerType;
    }

    private Klass createPredicateKlass() {
        var elementType = new TypeVariable(null, "Element", "T",
                DummyGenericDeclaration.INSTANCE);
        elementType.setBounds(List.of(new AnyType()));
        primTypeFactory.putType(Predicate.class.getTypeParameters()[0], elementType);
        var predicateType = ClassTypeBuilder.newBuilder("Predicate", "Predicate")
                .typeParameters(elementType)
                .source(ClassSource.BUILTIN)
                .kind(ClassKind.INTERFACE)
                .build();
        primTypeFactory.putType(Predicate.class, predicateType);
        MethodBuilder.newBuilder(predicateType, "test", "test")
                .returnType(StandardTypes.getBooleanType())
                .parameters(new Parameter(null, "element", "element", elementType.getType()))
                .build();
        return predicateType;
    }

    private void initBuiltinFlows() {
        var getSourceFunc = FunctionBuilder.newBuilder("getSource", "getSource")
                .isNative()
                .parameters(new Parameter(null, "view", "view", new AnyType()))
                .returnType(new AnyType())
                .build();
        NativeFunctions.setGetSourceFunc(getSourceFunc);
        defContext.writeEntity(getSourceFunc);

        var setSourceFunc = new FunctionBuilder("setSource", "setSource")
                .isNative()
                .parameters(
                        new Parameter(null, "view", "view", new AnyType()),
                        new Parameter(null, "source", "source", new AnyType())
                )
                .returnType(StandardTypes.getVoidType())
                .build();
        NativeFunctions.setSetSourceFunc(setSourceFunc);
        defContext.writeEntity(setSourceFunc);

        var isSourcePresentFunc = FunctionBuilder.newBuilder("isSourcePresent", "isSourcePresent")
                .isNative()
                .parameters(new Parameter(null, "view", "view", new AnyType()))
                .returnType(StandardTypes.getBooleanType())
                .build();
        NativeFunctions.setIsSourcePresent(isSourcePresentFunc);
        defContext.writeEntity(isSourcePresentFunc);

        var funcType = new TypeVariable(null, "FunctionType", "FunctionType", DummyGenericDeclaration.INSTANCE);
        var function2instance = FunctionBuilder.newBuilder("functionToInstance", "functionToInstance")
                .isNative()
                .typeParameters(List.of(funcType))
                .parameters(new Parameter(null, "function", "function", StandardTypes.getAnyType()))
                .returnType(funcType.getType())
                .build();
        NativeFunctions.setFunctionToInstance(function2instance);
        defContext.writeEntity(function2instance);

        var sendEmail = FunctionBuilder.newBuilder("sendEmail", "sendEmail")
                .isNative()
                .parameters(
                        new Parameter(null, "recipient", "recipient", StandardTypes.getStringType()),
                        new Parameter(null, "subject", "subject", StandardTypes.getStringType()),
                        new Parameter(null, "content", "content", StandardTypes.getStringType())
                )
                .returnType(StandardTypes.getVoidType())
                .build();
        NativeFunctions.setSendEmail(sendEmail);
        defContext.writeEntity(sendEmail);

        var getSessionEntry = FunctionBuilder.newBuilder("getSessionEntry", "getSessionEntry")
                .isNative()
                .parameters(
                        new Parameter(null, "key", "key", StandardTypes.getStringType())
                )
                .returnType(StandardTypes.getNullableAnyType())
                .build();
        NativeFunctions.setGetSessionEntry(getSessionEntry);
        defContext.writeEntity(getSessionEntry);

        var setSessionEntry = FunctionBuilder.newBuilder("setSessionEntry", "setSessionEntry")
                .isNative()
                .parameters(
                        new Parameter(null, "key", "key", StandardTypes.getStringType()),
                        new Parameter(null, "value", "value", StandardTypes.getAnyType())
                )
                .returnType(StandardTypes.getVoidType())
                .build();
        NativeFunctions.setSetSessionEntry(setSessionEntry);
        defContext.writeEntity(setSessionEntry);

        var removeSessionEntry = FunctionBuilder.newBuilder("removeSessionEntry", "removeSessionEntry")
                .isNative()
                .parameters(
                        new Parameter(null, "key", "key", StandardTypes.getStringType())
                )
                .returnType(StandardTypes.getBooleanType())
                .build();
        NativeFunctions.setRemoveSessionEntry(removeSessionEntry);
        defContext.writeEntity(removeSessionEntry);

        var castedType = new TypeVariable(null, "CastedType", "CastedType", DummyGenericDeclaration.INSTANCE);
        var typeCast = FunctionBuilder.newBuilder("typeCast", "typeCast")
                .isNative()
                .typeParameters(List.of(castedType))
                .parameters(
                        new Parameter(null, "instance", "instance", StandardTypes.getNullableAnyType())
                )
                .returnType(castedType.getType())
                .build();
        NativeFunctions.setTypeCast(typeCast);
        defContext.writeEntity(typeCast);

        var print = FunctionBuilder.newBuilder("print", "print")
                .isNative()
                .parameters(new Parameter(null, "content", "content", StandardTypes.getNullableAnyType()))
                .returnType(StandardTypes.getVoidType())
                .build();
        NativeFunctions.setPrint(print);
        defContext.writeEntity(print);

        var delete = FunctionBuilder.newBuilder("delete", "delete")
                .isNative()
                .parameters(new Parameter(null, "instance", "instance", StandardTypes.getAnyType()))
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

    private org.metavm.object.type.Field createField(Field javaField,
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
                .defaultValue(new NullInstance(StandardTypes.getNullType()))
                .staticValue(new NullInstance(StandardTypes.getNullType()))
                .build();
    }

    public ValueDef<Enum<?>> getEnumDef() {
        return enumDef;
    }

    private FieldDef createFieldDef(Field reflectField,
                                    org.metavm.object.type.Field field,
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

    public Klass getEnumType() {
        return enumDef.getTypeDef();
    }

    public org.metavm.object.type.Field getEnumNameField() {
        return enumNameDef.getField();
    }

    public org.metavm.object.type.Field getEnumOrdinalField() {
        return enumOrdinalDef.getField();
    }

    public Klass createIteratorKlass() {
        String name = getParameterizedName("Iterator");
        String code = getParameterizedCode("Iterator");
        var elementType = new TypeVariable(null, "IteratorElement", "IteratorElement",
                DummyGenericDeclaration.INSTANCE);
        elementType.setBounds(List.of(new AnyType()));
        primTypeFactory.putType(MetaIterator.class.getTypeParameters()[0], elementType);
        Klass iteratorType = ClassTypeBuilder.newBuilder(name, code)
                .typeParameters(elementType)
                .source(ClassSource.BUILTIN)
                .kind(ClassKind.INTERFACE).build();
        primTypeFactory.putType(MetaIterator.class, iteratorType);
        createIteratorFlows(iteratorType, elementType.getType());
        return iteratorType;
    }

    private void createIteratorFlows(Klass iteratorType, Type elementType) {
        boolean isAbstract = iteratorType.isInterface();
        boolean isNative = !iteratorType.isInterface();
        MethodBuilder.newBuilder(iteratorType, "hasNext", "hasNext")
                .isNative(isNative)
                .isAbstract(isAbstract)
                .returnType(StandardTypes.getBooleanType())
                .build();

        MethodBuilder.newBuilder(iteratorType, "next", "next")
                .isAbstract(isAbstract)
                .isNative(isNative)
                .returnType(elementType)
                .build();

        iteratorType.setStage(ResolutionStage.DEFINITION);
    }

    public Klass createIterableKlass() {
        var elementType = new TypeVariable(null, "Element", "T",
                DummyGenericDeclaration.INSTANCE);
        elementType.setBounds(List.of(new AnyType()));
        primTypeFactory.putType(MetaIterable.class.getTypeParameters()[0], elementType);
        var iterableType = ClassTypeBuilder.newBuilder("Iterable", "Iterable")
                .typeParameters(elementType)
                .source(ClassSource.BUILTIN)
                .kind(ClassKind.INTERFACE)
                .build();
        primTypeFactory.putType(MetaIterable.class, iterableType);
        createIterableFlows(iterableType, elementType);
        return iterableType;
    }

    private void createIterableFlows(Klass iterableType, TypeVariable elementType) {
        MethodBuilder.newBuilder(iterableType, "forEach", "forEach")
                .isNative(true)
                .returnType(StandardTypes.getVoidType())
                .parameters(new Parameter(null, "action", "action",
                        StandardTypes.getConsumerKlass().getParameterized(
                                List.of(new UncertainType(elementType.getType(), StandardTypes.getNullableAnyType()))).getType())
                )
                .build();

        var pIteratorType = StandardTypes.getIteratorKlass().getParameterized(List.of(elementType.getType()));
        MethodBuilder.newBuilder(iterableType, "iterator", "iterator")
                .isNative(true)
                .returnType(pIteratorType.getType())
                .build();

        iterableType.setStage(ResolutionStage.DEFINITION);
    }

    public Klass createCollectionType() {
        String name = getParameterizedName("Collection");
        String code = getParameterizedCode("Collection");
        var elementType = new TypeVariable(null, "CollectionElement", "CollectionElement",
                DummyGenericDeclaration.INSTANCE);
        var pIterableType = StandardTypes.getIterableKlass().getParameterized(List.of(elementType.getType()));
        elementType.setBounds(List.of(new AnyType()));
        primTypeFactory.putType(Collection.class.getTypeParameters()[0], elementType);
        Klass collectionType = ClassTypeBuilder.newBuilder(name, code)
                .typeParameters(elementType)
                .source(ClassSource.BUILTIN)
                .interfaces(pIterableType.getType())
                .kind(ClassKind.INTERFACE)
                .build();
        primTypeFactory.putType(Collection.class, collectionType);
        createCollectionFlows(collectionType, elementType);
        return collectionType;
    }

    private void createCollectionFlows(Klass collectionType, TypeVariable elementType) {
        MethodBuilder.newBuilder(collectionType, "size", "size")
                .isNative(true)
                .returnType(StandardTypes.getLongType())
                .build();

        MethodBuilder.newBuilder(collectionType, "isEmpty", "isEmpty")
                .isNative(true)
                .returnType(StandardTypes.getBooleanType())
                .build();

        MethodBuilder.newBuilder(collectionType, "contains", "contains")
                .isNative(true)
                .returnType(StandardTypes.getBooleanType())
                .parameters(new Parameter(null, "element", "element", StandardTypes.getAnyType()))
                .build();

        MethodBuilder.newBuilder(collectionType, "add", "add")
                .isNative(true)
                .returnType(StandardTypes.getBooleanType())
                .parameters(new Parameter(null, "element", "element", elementType.getType()))
                .build();

        MethodBuilder.newBuilder(collectionType, "addAll", "addAll")
                .isNative(true)
                .returnType(StandardTypes.getBooleanType())
                .parameters(new Parameter(null, "c", "c",
                        collectionType.getParameterized(List.of(new UncertainType(StandardTypes.getNeverType(), elementType.getType()))).getType()))
                .build();

        MethodBuilder.newBuilder(collectionType, "remove", "remove")
                .isNative(true)
                .returnType(StandardTypes.getBooleanType())
                .parameters(new Parameter(null, "element", "element", StandardTypes.getAnyType()))
                .build();

        MethodBuilder.newBuilder(collectionType, "clear", "clear")
                .isNative(true)
                .returnType(StandardTypes.getVoidType())
                .build();

        MethodBuilder.newBuilder(collectionType, "removeIf", "removeIf")
                .isNative(true)
                .returnType(StandardTypes.getBooleanType())
                .parameters(new Parameter(null, "filter", "filter",
                        StandardTypes.getPredicateKlass().getParameterized(
                                List.of(new UncertainType(elementType.getType(), StandardTypes.getNullableAnyType()))
                        ).getType()))
                .build();

        collectionType.setStage(ResolutionStage.DEFINITION);
    }

    public Klass createSetKlass() {
        String name = getParameterizedName("Set");
        String code = getParameterizedCode("Set");
        var elementType = new TypeVariable(null, "SetElement", "SetElement",
                DummyGenericDeclaration.INSTANCE);
        elementType.setBounds(List.of(new AnyType()));
        primTypeFactory.putType(MetaSet.class.getTypeParameters()[0], elementType);
        var pIterableType = StandardTypes.getIterableKlass().getParameterized(List.of(elementType.getType()));
        var pCollectionType = StandardTypes.getCollectionKlass().getParameterized(List.of(elementType.getType()));
        var pIteratorImplType = StandardTypes.getIteratorImplKlass().getParameterized(List.of(elementType.getType()));
        Klass setType = ClassTypeBuilder.newBuilder(name, code)
                .interfaces(pCollectionType.getType())
                .typeParameters(elementType)
                .source(ClassSource.BUILTIN)
                .dependencies(List.of(pIteratorImplType))
                .build();
        primTypeFactory.putType(MetaSet.class, setType);
        FieldBuilder.newBuilder("array", "array", setType, new ArrayType(elementType.getType(), ArrayKind.READ_WRITE))
                .nullType(StandardTypes.getNullType())
                .access(Access.PRIVATE)
                .isChild(true)
                .nullType(StandardTypes.getNullType())
                .build();
        createSetFlows(setType, pCollectionType, pIterableType);
        return setType;
    }

    private void createSetFlows(Klass setType, /*ClassType pSetType, */Klass collectionType, Klass iterableType) {
        MethodBuilder.newBuilder(setType, "Set", "Set")
                .isConstructor(true)
                .isNative(true)
                .returnType(setType.getType())
                .build();
        createOverridingFlows(setType, collectionType);
        createOverridingFlows(setType, iterableType);
        setType.setStage(ResolutionStage.DEFINITION);
    }

    public Klass createListKlass() {
        var elementType = new TypeVariable(null, "ListElement",
                "ListElement",
                DummyGenericDeclaration.INSTANCE);
        elementType.setBounds(List.of(new AnyType()));
        primTypeFactory.putType(MetaList.class.getTypeParameters()[0], elementType);
        var pCollectionType = StandardTypes.getCollectionKlass().getParameterized(List.of(elementType.getType()));
        var pIteratorImplType = StandardTypes.getIteratorImplKlass().getParameterized(List.of(elementType.getType()));
        var listType = ClassTypeBuilder.newBuilder("List", "List")
                .kind(ClassKind.INTERFACE)
                .interfaces(pCollectionType.getType())
                .typeParameters(elementType)
                .source(ClassSource.BUILTIN)
                .dependencies(List.of(pIteratorImplType))
                .build();
        primTypeFactory.putType(MetaList.class, listType);

        var nullableElementType = new UnionType(Set.of(elementType.getType(), StandardTypes.getNullType()));
        MethodBuilder.newBuilder(listType, "removeAt", "removeAt")
                .parameters(new Parameter(null, "index", "index", StandardTypes.getLongType()))
                .isNative(true)
                .returnType(nullableElementType)
                .build();

        MethodBuilder.newBuilder(listType, "get", "get")
                .parameters(new Parameter(null, "index", "index", StandardTypes.getLongType()))
                .isNative(true)
                .returnType(elementType.getType())
                .build();

        var uncertainType = new UncertainType(StandardTypes.getNeverType(), elementType.getType());
        var uncertainCollType = StandardTypes.getCollectionKlass().getParameterized(List.of(uncertainType));
        MethodBuilder.newBuilder(listType, listType.getName(), listType.getCode())
                .isConstructor(true)
                .isNative(true)
                .parameters(
                        new Parameter(null, "collection", "collection", uncertainCollType.getType())
                )
                .returnType(listType.getType())
                .build();

        MethodBuilder.newBuilder(listType, "set", "set")
                .parameters(
                        new Parameter(null, "index", "index", StandardTypes.getLongType()),
                        new Parameter(null, "value", "value", elementType.getType())
                )
                .isNative(true)
                .returnType(nullableElementType)
                .build();

        MethodBuilder.newBuilder(listType, "of", "of")
                .isStatic(true)
                .isNative(true)
                .parameters(
                        new Parameter(null, "elements", "elements", new ArrayType(elementType.getType(), ArrayKind.READ_ONLY))
                )
                .returnType(listType.getType())
                .build();

        listType.setStage(ResolutionStage.DEFINITION);
        return listType;
    }

    public Klass createReadWriteListKlass() {
        return createListImplKlass("ReadWriteList", "ReadWriteList", ReadWriteMetaList.class, ClassKind.CLASS, ArrayKind.READ_WRITE);
    }

    public Klass createChildListKlass() {
        return createListImplKlass("ChildList", "ChildList", ChildMetaList.class, ClassKind.CLASS, ArrayKind.CHILD);
    }

    public Klass createValueListKlass() {
        return createListImplKlass("ValueList", "ValueList", ValueMetaList.class, ClassKind.VALUE, ArrayKind.VALUE);
    }

    public Klass createListImplKlass(String name, String code, Class<?> javaClass, ClassKind kind, ArrayKind arrayKind) {
        var elementType = new TypeVariable(null, name + "Element", code + "Element",
                DummyGenericDeclaration.INSTANCE);
        elementType.setBounds(List.of(new AnyType()));
        primTypeFactory.putType(javaClass.getTypeParameters()[0], elementType);
        var pIterableType = StandardTypes.getIterableKlass().getParameterized(List.of(elementType.getType()));
        var pCollectionType = StandardTypes.getCollectionKlass().getParameterized(List.of(elementType.getType()));
        var pListType = StandardTypes.getListKlass().getParameterized(List.of(elementType.getType()));
        var pIteratorImplType = StandardTypes.getIteratorImplKlass().getParameterized(List.of(elementType.getType()));
        var listImplType = ClassTypeBuilder.newBuilder(name, code)
                .kind(kind)
                .interfaces(pListType.getType())
                .typeParameters(elementType)
                .source(ClassSource.BUILTIN)
                .dependencies(List.of(pIteratorImplType))
                .build();
        primTypeFactory.putType(javaClass, listImplType);
        FieldBuilder.newBuilder("array", "array", listImplType,
                        new ArrayType(elementType.getType(), arrayKind))
                .nullType(StandardTypes.getNullType())
                .access(Access.PRIVATE)
                .isChild(kind != ClassKind.VALUE)
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
                                new ClassType(
                                        StandardTypes.getCollectionKlass(),
                                        List.of(new UncertainType(StandardTypes.getNeverType(), elementType.getType()))
                                )
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
        var uncertainType = new UncertainType(StandardTypes.getNeverType(), elementType.getType());
        var uncertainCollType = StandardTypes.getCollectionKlass().getParameterized(List.of(uncertainType));
        MethodBuilder.newBuilder(listType, listType.getName(), listType.getCode())
                .isConstructor(true)
                .isNative(true)
                .parameters(
                        new Parameter(null, "collection", "collection", uncertainCollType.getType())
                )
                .returnType(listType.getType())
                .build();

        var nullableElementType = new UnionType(Set.of(elementType.getType(), StandardTypes.getNullType()));
        MethodBuilder.newBuilder(listType, "set", "set")
                .parameters(
                        new Parameter(null, "index", "index", StandardTypes.getLongType()),
                        new Parameter(null, "value", "value", elementType.getType())
                )
                .isNative(true)
                .returnType(nullableElementType)
                .build();

        listType.setStage(ResolutionStage.DEFINITION);
    }

    public Klass createIteratorImplKlass() {
        String name = getParameterizedName("IteratorImpl");
        String code = getParameterizedCode("IteratorImpl");
        var elementType = new TypeVariable(null, "IteratorImplElement", "IteratorImplElement",
                DummyGenericDeclaration.INSTANCE);
        elementType.setBounds(List.of(new AnyType()));
        primTypeFactory.putType(IteratorImpl.class.getTypeParameters()[0], elementType);
        var pIteratorType = StandardTypes.getIteratorKlass().getParameterized(List.of(elementType.getType()));
        Klass iteratorImplType = ClassTypeBuilder.newBuilder(name, code)
                .interfaces(List.of(pIteratorType.getType()))
                .typeParameters(elementType)
                .source(ClassSource.BUILTIN)
                .build();
        primTypeFactory.putType(IteratorImpl.class, iteratorImplType);
        var pCollectionType = StandardTypes.getCollectionKlass().getParameterized(List.of(elementType.getType()));
        MethodBuilder.newBuilder(iteratorImplType, "IteratorImpl", "IteratorImpl")
                .isConstructor(true)
                .isNative(true)
                .returnType(iteratorImplType.getType())
                .parameters(new Parameter(null, "collection", "collection", pCollectionType.getType()))
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

    public Klass createMapKlass() {
        String name = getParameterizedName("Map");
        String code = getParameterizedName("Map");
        var keyType = new TypeVariable(null, "MapKey", "MapKey",
                DummyGenericDeclaration.INSTANCE);
        keyType.setBounds(List.of(new AnyType()));
        primTypeFactory.putType(MetaMap.class.getTypeParameters()[0], keyType);
        var valueType = new TypeVariable(null, "MapValue", "MapValue",
                DummyGenericDeclaration.INSTANCE);
        valueType.setBounds(List.of(new AnyType()));
        primTypeFactory.putType(MetaMap.class.getTypeParameters()[1], valueType);
        var pSetType = StandardTypes.getSetKlass().getParameterized(List.of(keyType.getType()));
        Klass mapType = ClassTypeBuilder.newBuilder(name, code)
                .source(ClassSource.BUILTIN)
                .dependencies(List.of(pSetType))
                .typeParameters(keyType, valueType)
                .build();
        primTypeFactory.putType(MetaMap.class, mapType);
        FieldBuilder
                .newBuilder("keyArray", "keyArray", mapType, new ArrayType(keyType.getType(), ArrayKind.READ_WRITE))
                .access(Access.PRIVATE)
                .isChild(true)
                .nullType(StandardTypes.getNullType())
                .build();
        FieldBuilder
                .newBuilder("valueArray", "valueArray", mapType, new ArrayType(valueType.getType(), ArrayKind.READ_WRITE))
                .access(Access.PRIVATE)
                .isChild(true)
                .nullType(StandardTypes.getNullType())
                .build();
        createMapFlows(mapType, keyType.getType(), valueType.getType());
        return mapType;
    }

    private void createMapFlows(Klass mapType, Type keyType, Type valueType) {
        MethodBuilder.newBuilder(mapType, "Map", "Map")
                .isConstructor(true)
                .isNative(true)
                .returnType(mapType.getType())
                .build();

        var nullableValueType = new UnionType(Set.of(valueType, StandardTypes.getNullType()));

        MethodBuilder.newBuilder(mapType, "put", "put")
                .isNative(true)
                .returnType(nullableValueType)
                .parameters(new Parameter(null, "key", "key", keyType),
                        new Parameter(null, "value", "value", valueType))
                .build();

        MethodBuilder.newBuilder(mapType, "get", "get")
                .isNative(true)
                .returnType(nullableValueType)
                .parameters(new Parameter(null, "key", "key", keyType))
                .build();

        MethodBuilder.newBuilder(mapType, "remove", "remove")
                .isNative(true)
                .returnType(nullableValueType)
                .parameters(new Parameter(null, "key", "key", keyType))
                .build();

        MethodBuilder.newBuilder(mapType, "size", "size")
                .isNative(true)
                .returnType(StandardTypes.getLongType())
                .build();

        MethodBuilder.newBuilder(mapType, "clear", "clear")
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
                .parameters(new Parameter(null, "message", "message", StandardTypes.getStringType()))
                .build();

        MethodBuilder.newBuilder(throwableType, "Throwable", "Throwable")
                .isConstructor(true)
                .isNative(true)
                .returnType(throwableType.getType())
                .parameters(new Parameter(null, "cause", "cause", throwableType.getType()))
                .build();

        MethodBuilder.newBuilder(throwableType, "Throwable", "Throwable")
                .isConstructor(true)
                .isNative(true)
                .returnType(throwableType.getType())
                .parameters(
                        new Parameter(null, "message", "message", StandardTypes.getStringType()),
                        new Parameter(null, "cause", "cause", throwableType.getType())
                )
                .build();

        MethodBuilder.newBuilder(throwableType, "getMessage", "getMessage")
                .isNative(true)
                .returnType(StandardTypes.getNullableType(StandardTypes.getStringType()))
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
                .parameters(new Parameter(null, "message", "message", StandardTypes.getStringType()))
                .build();

        MethodBuilder.newBuilder(runtimeExceptionType, name, code)
                .isConstructor(true)
                .isNative(true)
                .returnType(runtimeExceptionType.getType())
                .parameters(new Parameter(null, "cause", "cause", StandardTypes.getThrowableKlass().getType()))
                .build();

        MethodBuilder.newBuilder(runtimeExceptionType, name, code)
                .isConstructor(true)
                .isNative(true)
                .returnType(runtimeExceptionType.getType())
                .parameters(
                        new Parameter(null, "message", "message", StandardTypes.getStringType()),
                        new Parameter(null, "cause", "cause", StandardTypes.getThrowableKlass().getType())
                )
                .build();
    }


    private static class PrimTypeFactory extends TypeFactory {

        private final Map<java.lang.reflect.Type, TypeDef> javaType2TypeDef = new HashMap<>();
        private final Map<TypeDef, java.lang.reflect.Type> typeDef2JavaType = new IdentityHashMap<>();

        @Override
        public void putType(java.lang.reflect.Type javaType, TypeDef typeDef) {
            NncUtils.requireFalse(javaType2TypeDef.containsKey(javaType));
            NncUtils.requireFalse(typeDef2JavaType.containsKey(typeDef));
            javaType2TypeDef.put(javaType, typeDef);
            typeDef2JavaType.put(typeDef, javaType);
        }

        public Map<java.lang.reflect.Type, TypeDef> getMap() {
            return Collections.unmodifiableMap(javaType2TypeDef);
        }

        public void saveDefs(DefMap defMap) {
            for (var typeDef : javaType2TypeDef.values()) {
                createDefIfAbsent(typeDef, defMap);
            }
            for (var typeDef : javaType2TypeDef.values()) {
                defMap.afterDefInitialized(defMap.getDef(typeDef));
            }
        }

        private ModelDef<?, ?> createDefIfAbsent(TypeDef typeDef, DefMap defMap) {
            if (defMap.containsDef(typeDef)) {
                return defMap.getDef(typeDef);
            }
            var javaType = NncUtils.requireNonNull(typeDef2JavaType.get(typeDef));
            var def = switch (typeDef) {
                case TypeVariable typeVariable -> new TypeVariableDef(
                        (java.lang.reflect.TypeVariable<?>) javaType, typeVariable
                );
                default -> new DirectDef<>(javaType, typeDef, NATIVE_CLASS_MAP.get(javaType));
            };
            defMap.preAddDef(def);
            return def;
        }


    }

}
