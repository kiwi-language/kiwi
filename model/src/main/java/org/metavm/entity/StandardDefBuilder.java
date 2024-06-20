package org.metavm.entity;

import org.metavm.api.ChildList;
import org.metavm.api.ValueList;
import org.metavm.entity.natives.StdFunction;
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

    private Klass iteratorKlass;

    private Klass iteratorImplKlass;

    private Klass iterableKlass;

    private Klass throwableKlass;

    private Klass collectionKlass;

    private Klass listKlass;

    public Klass setKlass;

    public Klass mapKlass;

    private Klass consumerKlass;

    private Klass predicateKlass;

    private final PrimTypeFactory primTypeFactory = new PrimTypeFactory();

    public StandardDefBuilder(DefContext defContext) {
        this.defContext = defContext;
    }

    public void initRootTypes() {
        initSystemFunctions();
        consumerKlass = createConsumerKlass();
        predicateKlass = createPredicateKlass();
        iteratorKlass = createIteratorKlass();
        iterableKlass = createIterableKlass();
        collectionKlass = createCollectionKlass();
        iteratorImplKlass = createIteratorImplKlass();
        setKlass = createSetKlass();
        listKlass = createListKlass();
        mapKlass = createMapKlass();
        createReadWriteListKlass();
        createChildListKlass();
        createValueListKlass();
        createHashSetKlass();
        createHashMapKlass();

        ValueDef<Record> recordDef = createValueDef(
                Record.class,
                Record.class,
                KlassBuilder.newBuilder("Record", Record.class.getSimpleName())
                        .source(ClassSource.BUILTIN)
                        .kind(ClassKind.VALUE).build(),
                defContext
        );
        defContext.addDef(recordDef);

        var entityKlass = KlassBuilder.newBuilder("Entity", Entity.class.getSimpleName())
                .source(ClassSource.BUILTIN)
                .build();
        EntityDef<Entity> entityDef = createEntityDef(
                Entity.class,
                Entity.class,
                entityKlass,
                defContext
        );

        defContext.addDef(entityDef);

        var enumTypeParam = new TypeVariable(null, "EnumType", "EnumType",
                DummyGenericDeclaration.INSTANCE);
        var enumKlass = KlassBuilder.newBuilder("Enum", Enum.class.getSimpleName())
                .source(ClassSource.BUILTIN)
                .typeParameters(enumTypeParam)
                .build();

        enumTypeParam.setBounds(List.of(enumKlass.getType()));

        enumDef = createValueDef(
                Enum.class,// Enum is not a RuntimeGeneric, use the raw class
                new TypeReference<Enum<?>>() {
                }.getType(),
                enumKlass,
                defContext
        );

        enumNameDef = createFieldDef(
                ENUM_NAME_FIELD,
                createField(ENUM_NAME_FIELD, true, Types.getStringType(), Access.PUBLIC,
                        ColumnKind.STRING.getColumn(0), enumKlass),
                enumDef
        );

        enumOrdinalDef = createFieldDef(
                ENUM_ORDINAL_FIELD,
                createField(ENUM_ORDINAL_FIELD, false, Types.getLongType(), Access.PRIVATE,
                        ColumnKind.INT.getColumn(0), enumKlass),
                enumDef
        );
        enumKlass.setTitleField(enumNameDef.getField());
        enumKlass.setStage(ResolutionStage.DEFINITION);

        var enumTypeParamDef = new TypeVariableDef(Enum.class.getTypeParameters()[0], enumTypeParam);
        defContext.preAddDef(enumTypeParamDef);
        defContext.addDef(enumDef);
        defContext.afterDefInitialized(enumTypeParamDef);

        primTypeFactory.saveDefs(defContext);

        primTypeFactory.getMap().keySet().forEach(javaType ->
                defContext.afterDefInitialized(defContext.getDef(javaType))
        );

        throwableKlass = KlassBuilder.newBuilder("Throwable", Throwable.class.getSimpleName())
                .source(ClassSource.BUILTIN).build();
        createThrowableFlows(throwableKlass);
        var throwableDef = createValueDef(
                Throwable.class,
                Throwable.class,
                throwableKlass,
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
                        Types.getNullableType(Types.getStringType()), Access.PUBLIC,
                        ColumnKind.STRING.getColumn(0), throwableKlass),
                throwableDef
        );

        var javaCauseField = ReflectionUtils.getField(Throwable.class, "cause");
        createFieldDef(
                javaCauseField,
                createField(javaCauseField, false,
                        Types.getNullableType(throwableKlass.getType()), Access.PUBLIC,
                        ColumnKind.REFERENCE.getColumn(0), throwableKlass),
                throwableDef
        );
        defContext.afterDefInitialized(throwableDef);
        var exceptionKlass = KlassBuilder.newBuilder("Exception", Exception.class.getSimpleName())
                .superClass(throwableKlass.getType())
                .source(ClassSource.BUILTIN).build();

        createExceptionFlows(exceptionKlass);
//        defContext.addDef(createValueDef(Exception.class, Exception.class, exceptionType, defContext));
        defContext.addDef(new DirectDef<>(Exception.class, exceptionKlass));

        var runtimeExceptionKlass = KlassBuilder.newBuilder("RuntimeException", RuntimeException.class.getSimpleName())
                .superClass(exceptionKlass.getType())
                .source(ClassSource.BUILTIN).build();
        createRuntimeExceptionFlows(runtimeExceptionKlass);
        defContext.addDef(new DirectDef<>(
                RuntimeException.class, runtimeExceptionKlass));

        var illegalArgumentExceptionKlass =  KlassBuilder.newBuilder("IllegalArgumentException", IllegalArgumentException.class.getSimpleName())
                .superClass(runtimeExceptionKlass.getType())
                .source(ClassSource.BUILTIN).build();
        createIllegalArgumentExceptionFlows(illegalArgumentExceptionKlass);
        defContext.addDef(new DirectDef<>(
                IllegalArgumentException.class, illegalArgumentExceptionKlass));

        var illegalStateExceptionKlass = KlassBuilder.newBuilder("IllegalStateException", IllegalStateException.class.getSimpleName())
                .superClass(runtimeExceptionKlass.getType())
                .source(ClassSource.BUILTIN).build();
        createIllegalStateExceptionFlows(illegalStateExceptionKlass);
        defContext.addDef(new DirectDef<>(
                IllegalStateException.class, illegalStateExceptionKlass));

        var nullPointerExceptionKlass = KlassBuilder.newBuilder("NullPointerException", NullPointerException.class.getSimpleName())
                .superClass(runtimeExceptionKlass.getType())
                .source(ClassSource.BUILTIN).build();
        createNullPointerExceptionFlows(nullPointerExceptionKlass);
        defContext.addDef(new DirectDef<>(
                NullPointerException.class, nullPointerExceptionKlass));
    }

    private Klass createConsumerKlass() {
        var elementType = new TypeVariable(null, "Element", "T",
                DummyGenericDeclaration.INSTANCE);
        elementType.setBounds(List.of(AnyType.instance));
        primTypeFactory.putType(Consumer.class.getTypeParameters()[0], elementType);
        var consumerType = KlassBuilder.newBuilder("Consumer", "Consumer")
                .typeParameters(elementType)
                .source(ClassSource.BUILTIN)
                .kind(ClassKind.INTERFACE)
                .build();
        primTypeFactory.putType(Consumer.class, consumerType);
        MethodBuilder.newBuilder(consumerType, "accept", "accept")
                .returnType(Types.getVoidType())
                .parameters(new Parameter(null, "element", "element", elementType.getType()))
                .build();
        return consumerType;
    }

    private Klass createPredicateKlass() {
        var elementType = new TypeVariable(null, "Element", "T",
                DummyGenericDeclaration.INSTANCE);
        elementType.setBounds(List.of(AnyType.instance));
        primTypeFactory.putType(Predicate.class.getTypeParameters()[0], elementType);
        var predicateType = KlassBuilder.newBuilder("Predicate", "Predicate")
                .typeParameters(elementType)
                .source(ClassSource.BUILTIN)
                .kind(ClassKind.INTERFACE)
                .build();
        primTypeFactory.putType(Predicate.class, predicateType);
        MethodBuilder.newBuilder(predicateType, "test", "test")
                .returnType(Types.getBooleanType())
                .parameters(new Parameter(null, "element", "element", elementType.getType()))
                .build();
        return predicateType;
    }

    private void initSystemFunctions() {
        StdFunction.defineSystemFunctions().forEach(defContext::writeEntity);
    }

    public void initUserFunctions() {
        StdFunction.defineUserFunctions(defContext).forEach(defContext::writeEntity);
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
                .defaultValue(new NullInstance(Types.getNullType()))
                .staticValue(new NullInstance(Types.getNullType()))
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
        elementType.setBounds(List.of(AnyType.instance));
        primTypeFactory.putType(Iterator.class.getTypeParameters()[0], elementType);
        Klass iteratorType = KlassBuilder.newBuilder(name, code)
                .typeParameters(elementType)
                .source(ClassSource.BUILTIN)
                .kind(ClassKind.INTERFACE).build();
        primTypeFactory.putType(Iterator.class, iteratorType);
        createIteratorFlows(iteratorType, elementType.getType());
        return iteratorType;
    }

    private void createIteratorFlows(Klass iteratorType, Type elementType) {
        boolean isAbstract = iteratorType.isInterface();
        boolean isNative = !iteratorType.isInterface();
        MethodBuilder.newBuilder(iteratorType, "hasNext", "hasNext")
                .isNative(isNative)
                .isAbstract(isAbstract)
                .returnType(Types.getBooleanType())
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
        elementType.setBounds(List.of(AnyType.instance));
        primTypeFactory.putType(Iterable.class.getTypeParameters()[0], elementType);
        var iterableType = KlassBuilder.newBuilder("Iterable", "Iterable")
                .typeParameters(elementType)
                .source(ClassSource.BUILTIN)
                .kind(ClassKind.INTERFACE)
                .build();
        primTypeFactory.putType(Iterable.class, iterableType);
        createIterableFlows(iterableType, elementType);
        return iterableType;
    }

    private void createIterableFlows(Klass iterableType, TypeVariable elementType) {
        MethodBuilder.newBuilder(iterableType, "forEach", "forEach")
                .isNative(true)
                .returnType(Types.getVoidType())
                .parameters(new Parameter(null, "action", "action",
                        consumerKlass.getParameterized(
                                List.of(UncertainType.createLowerBounded(elementType.getType()))).getType())
                )
                .build();

        var pIteratorType = iteratorKlass.getParameterized(List.of(elementType.getType()));
        MethodBuilder.newBuilder(iterableType, "iterator", "iterator")
                .isNative(true)
                .returnType(pIteratorType.getType())
                .build();

        iterableType.setStage(ResolutionStage.DEFINITION);
    }

    public Klass createCollectionKlass() {
        String name = getParameterizedName("Collection");
        String code = getParameterizedCode("Collection");
        var elementType = new TypeVariable(null, "E", "E",
                DummyGenericDeclaration.INSTANCE);
        var pIterableType = iterableKlass.getParameterized(List.of(elementType.getType()));
        elementType.setBounds(List.of(AnyType.instance));
        primTypeFactory.putType(Collection.class.getTypeParameters()[0], elementType);
        Klass collectionType = KlassBuilder.newBuilder(name, code)
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
                .returnType(Types.getLongType())
                .build();

        MethodBuilder.newBuilder(collectionType, "isEmpty", "isEmpty")
                .isNative(true)
                .returnType(Types.getBooleanType())
                .build();

        MethodBuilder.newBuilder(collectionType, "contains", "contains")
                .isNative(true)
                .returnType(Types.getBooleanType())
                .parameters(new Parameter(null, "element", "element", Types.getAnyType()))
                .build();

        MethodBuilder.newBuilder(collectionType, "add", "add")
                .isNative(true)
                .returnType(Types.getBooleanType())
                .parameters(new Parameter(null, "element", "element", elementType.getType()))
                .build();

        MethodBuilder.newBuilder(collectionType, "addAll", "addAll")
                .isNative(true)
                .returnType(Types.getBooleanType())
                .parameters(new Parameter(null, "c", "c",
                        collectionType.getParameterized(List.of(UncertainType.createUpperBounded(elementType.getType()))).getType()))
                .build();

        MethodBuilder.newBuilder(collectionType, "remove", "remove")
                .isNative(true)
                .returnType(Types.getBooleanType())
                .parameters(new Parameter(null, "element", "element", Types.getAnyType()))
                .build();

        MethodBuilder.newBuilder(collectionType, "clear", "clear")
                .isNative(true)
                .returnType(Types.getVoidType())
                .build();

        MethodBuilder.newBuilder(collectionType, "removeIf", "removeIf")
                .isNative(true)
                .returnType(Types.getBooleanType())
                .parameters(new Parameter(null, "filter", "filter",
                        predicateKlass.getParameterized(
                                List.of(UncertainType.createLowerBounded(elementType.getType()))
                        ).getType()))
                .build();

        collectionType.setStage(ResolutionStage.DEFINITION);
    }

    public Klass createSetKlass() {
        String name = getParameterizedName("Set");
        String code = getParameterizedCode("Set");
        var elementType = new TypeVariable(null, "E", "E",
                DummyGenericDeclaration.INSTANCE);
        elementType.setBounds(List.of(AnyType.instance));
        primTypeFactory.putType(Set.class.getTypeParameters()[0], elementType);
        var pIterableType = iterableKlass.getParameterized(List.of(elementType.getType()));
        var pCollectionType = collectionKlass.getParameterized(List.of(elementType.getType()));
        Klass setType = KlassBuilder.newBuilder(name, code)
                .kind(ClassKind.INTERFACE)
                .interfaces(pCollectionType.getType())
                .typeParameters(elementType)
                .source(ClassSource.BUILTIN)
                .build();
        primTypeFactory.putType(Set.class, setType);
        FieldBuilder.newBuilder("array", "array", setType, new ArrayType(elementType.getType(), ArrayKind.READ_WRITE))
                .access(Access.PRIVATE)
                .isChild(true)
                .build();
        createSetFlows(setType, pCollectionType, pIterableType);
        return setType;
    }

    private void createSetFlows(Klass setType, /*ClassType pSetType, */Klass collectionType, Klass iterableType) {
//        MethodBuilder.newBuilder(setType, "Set", "Set")
//                .isConstructor(true)
//                .isNative(true)
//                .returnType(setType.getType())
//                .build();
//        createOverridingFlows(setType, collectionType);
//        createOverridingFlows(setType, iterableType);
//        setType.setStage(ResolutionStage.DEFINITION);
    }

    public Klass createListKlass() {
        var elementType = new TypeVariable(null, "E", "E",
                DummyGenericDeclaration.INSTANCE);
        elementType.setBounds(List.of(AnyType.instance));
        primTypeFactory.putType(List.class.getTypeParameters()[0], elementType);
        var pCollectionType = collectionKlass.getParameterized(List.of(elementType.getType()));
        var listType = KlassBuilder.newBuilder("List", "List")
                .kind(ClassKind.INTERFACE)
                .interfaces(pCollectionType.getType())
                .typeParameters(elementType)
                .source(ClassSource.BUILTIN)
                .build();
        primTypeFactory.putType(List.class, listType);

        var nullableElementType = new UnionType(Set.of(elementType.getType(), Types.getNullType()));
        MethodBuilder.newBuilder(listType, "removeAt", "removeAt")
                .parameters(new Parameter(null, "index", "index", Types.getLongType()))
                .returnType(nullableElementType)
                .build();

        MethodBuilder.newBuilder(listType, "get", "get")
                .parameters(new Parameter(null, "index", "index", Types.getLongType()))
                .returnType(elementType.getType())
                .build();

        MethodBuilder.newBuilder(listType, "set", "set")
                .parameters(
                        new Parameter(null, "index", "index", Types.getLongType()),
                        new Parameter(null, "value", "value", elementType.getType())
                )
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
        return createListImplKlass("ReadWriteList", "ReadWriteList", ArrayList.class, ClassKind.CLASS, ArrayKind.READ_WRITE);
    }

    public Klass createChildListKlass() {
        return createListImplKlass("ChildList", "ChildList", ChildList.class, ClassKind.CLASS, ArrayKind.CHILD);
    }

    public Klass createValueListKlass() {
        return createListImplKlass("ValueList", "ValueList", ValueList.class, ClassKind.VALUE, ArrayKind.VALUE);
    }

    public Klass createListImplKlass(String name, String code, Class<?> javaClass, ClassKind kind, ArrayKind arrayKind) {
        var elementType = new TypeVariable(null, "E", "E",
                DummyGenericDeclaration.INSTANCE);
        elementType.setBounds(List.of(AnyType.instance));
        primTypeFactory.putType(javaClass.getTypeParameters()[0], elementType);
        var pIterableType = iterableKlass.getParameterized(List.of(elementType.getType()));
        var pCollectionType = collectionKlass.getParameterized(List.of(elementType.getType()));
        var pListType = listKlass.getParameterized(List.of(elementType.getType()));
        var listImplType = KlassBuilder.newBuilder(name, code)
                .kind(kind)
                .interfaces(pListType.getType())
                .typeParameters(elementType)
                .source(ClassSource.BUILTIN)
                .build();
        primTypeFactory.putType(javaClass, listImplType);
        FieldBuilder.newBuilder("array", "array", listImplType,
                        new ArrayType(elementType.getType(), arrayKind))
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
                                        collectionKlass,
                                        List.of(UncertainType.createUpperBounded(elementType.getType()))
                                )
                        )
                )
                .returnType(listImplType.getType())
                .build();
        return listImplType;
    }

    public Klass createHashSetKlass() {
        return createSetImplKlass(HashSet.class, ClassKind.CLASS, ArrayKind.READ_WRITE);
    }

    public Klass createSetImplKlass(Class<?> javaClass, ClassKind kind, ArrayKind arrayKind) {
        var elementType = new TypeVariable(null, "E", "E",
                DummyGenericDeclaration.INSTANCE);
        elementType.setBounds(List.of(AnyType.instance));
        primTypeFactory.putType(javaClass.getTypeParameters()[0], elementType);
        var pIterableType = iterableKlass.getParameterized(List.of(elementType.getType()));
        var pCollectionType = collectionKlass.getParameterized(List.of(elementType.getType()));
        var pSetKlass = setKlass.getParameterized(List.of(elementType.getType()));
        var setImplKlass = KlassBuilder.newBuilder(javaClass.getSimpleName(), javaClass.getName())
                .kind(kind)
                .interfaces(pSetKlass.getType())
                .typeParameters(elementType)
                .source(ClassSource.BUILTIN)
                .build();
        primTypeFactory.putType(javaClass, setImplKlass);
        FieldBuilder.newBuilder("array", "array", setImplKlass,
                        new ArrayType(elementType.getType(), arrayKind))
                .access(Access.PRIVATE)
                .isChild(kind != ClassKind.VALUE)
                .build();
        createOverridingFlows(setImplKlass, pIterableType);
        createOverridingFlows(setImplKlass, pCollectionType);
        createOverridingFlows(setImplKlass, pSetKlass);

        var constructorName = javaClass.getSimpleName();
        MethodBuilder.newBuilder(setImplKlass, constructorName, constructorName)
                .isConstructor(true)
                .isNative(true)
                .returnType(setImplKlass.getType())
                .build();

        MethodBuilder.newBuilder(setImplKlass, constructorName, constructorName)
                .isConstructor(true)
                .isNative(true)
                .parameters(
                        new Parameter(
                                null, "collection", "collection",
                                new ClassType(
                                        collectionKlass,
                                        List.of(UncertainType.createUpperBounded(elementType.getType()))
                                )
                        )
                )
                .returnType(setImplKlass.getType())
                .build();
        return setImplKlass;
    }

    public Klass createIteratorImplKlass() {
        String name = getParameterizedName("IteratorImpl");
        String code = getParameterizedCode("IteratorImpl");
        var elementType = new TypeVariable(null, "IteratorImplElement", "IteratorImplElement",
                DummyGenericDeclaration.INSTANCE);
        elementType.setBounds(List.of(AnyType.instance));
        primTypeFactory.putType(IteratorImpl.class.getTypeParameters()[0], elementType);
        var pIteratorType = iteratorKlass.getParameterized(List.of(elementType.getType()));
        Klass iteratorImplType = KlassBuilder.newBuilder(name, code)
                .interfaces(List.of(pIteratorType.getType()))
                .typeParameters(elementType)
                .source(ClassSource.BUILTIN)
                .build();
        primTypeFactory.putType(IteratorImpl.class, iteratorImplType);
        var pCollectionType = collectionKlass.getParameterized(List.of(elementType.getType()));
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
        var keyType = new TypeVariable(null, "K", "K",
                DummyGenericDeclaration.INSTANCE);
        keyType.setBounds(List.of(AnyType.instance));
        primTypeFactory.putType(Map.class.getTypeParameters()[0], keyType);
        var valueType = new TypeVariable(null, "V", "V",
                DummyGenericDeclaration.INSTANCE);
        valueType.setBounds(List.of(AnyType.instance));
        primTypeFactory.putType(Map.class.getTypeParameters()[1], valueType);
        Klass mapType = KlassBuilder.newBuilder(name, code)
                .source(ClassSource.BUILTIN)
                .kind(ClassKind.INTERFACE)
                .typeParameters(keyType, valueType)
                .build();
        primTypeFactory.putType(Map.class, mapType);
        createMapFlows(mapType, keyType.getType(), valueType.getType());
        return mapType;
    }

    private Klass createHashMapKlass() {
        return createMapImplKlass(HashMap.class, ClassKind.CLASS, ArrayKind.READ_WRITE);
    }

    private Klass createMapImplKlass(Class<?> javaClass, ClassKind kind, ArrayKind valueArrayKind) {
        var keyTypeVar = new TypeVariable(null, "E", "E",
                DummyGenericDeclaration.INSTANCE);
        var valueTypeVar = new TypeVariable(null, "V", "V",
                DummyGenericDeclaration.INSTANCE);
        keyTypeVar.setBounds(List.of(AnyType.instance));
        primTypeFactory.putType(javaClass.getTypeParameters()[0], keyTypeVar);
        primTypeFactory.putType(javaClass.getTypeParameters()[1], valueTypeVar);
        var pMapKlass = mapKlass.getParameterized(List.of(keyTypeVar.getType(), valueTypeVar.getType()));
        var mapImplKlass = KlassBuilder.newBuilder(javaClass.getSimpleName(), javaClass.getName())
                .kind(kind)
                .interfaces(pMapKlass.getType())
                .typeParameters(keyTypeVar, valueTypeVar)
                .source(ClassSource.BUILTIN)
                .build();
        primTypeFactory.putType(javaClass, mapImplKlass);
        FieldBuilder.newBuilder("keyArray", "keyArray", mapImplKlass,
                        new ArrayType(keyTypeVar.getType(), ArrayKind.READ_WRITE))
                .access(Access.PRIVATE)
                .isChild(kind != ClassKind.VALUE)
                .build();
        FieldBuilder.newBuilder("valueArray", "valueArray", mapImplKlass,
                        new ArrayType(valueTypeVar.getType(), valueArrayKind))
                .access(Access.PRIVATE)
                .isChild(kind != ClassKind.VALUE)
                .build();
        createOverridingFlows(mapImplKlass, pMapKlass);

        var constructorName = javaClass.getSimpleName();
        MethodBuilder.newBuilder(mapImplKlass, constructorName, constructorName)
                .isConstructor(true)
                .isNative(true)
                .returnType(mapImplKlass.getType())
                .build();

        return mapImplKlass;
    }

    private void createMapFlows(Klass mapType, Type keyType, Type valueType) {
        var nullableValueType = new UnionType(Set.of(valueType, Types.getNullType()));

        MethodBuilder.newBuilder(mapType, "put", "put")
                .returnType(nullableValueType)
                .parameters(new Parameter(null, "key", "key", keyType),
                        new Parameter(null, "value", "value", valueType))
                .build();

        MethodBuilder.newBuilder(mapType, "get", "get")
                .returnType(nullableValueType)
                .parameters(new Parameter(null, "key", "key", keyType))
                .build();

        MethodBuilder.newBuilder(mapType, "remove", "remove")
                .returnType(nullableValueType)
                .parameters(new Parameter(null, "key", "key", keyType))
                .build();

        MethodBuilder.newBuilder(mapType, "size", "size")
                .returnType(Types.getLongType())
                .build();

        MethodBuilder.newBuilder(mapType, "clear", "clear")
                .returnType(Types.getVoidType())
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
                .parameters(new Parameter(null, "message", "message", Types.getStringType()))
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
                        new Parameter(null, "message", "message", Types.getStringType()),
                        new Parameter(null, "cause", "cause", throwableType.getType())
                )
                .build();

        MethodBuilder.newBuilder(throwableType, "getMessage", "getMessage")
                .isNative(true)
                .returnType(Types.getNullableType(Types.getStringType()))
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
                .parameters(new Parameter(null, "message", "message", Types.getStringType()))
                .build();

        MethodBuilder.newBuilder(runtimeExceptionType, name, code)
                .isConstructor(true)
                .isNative(true)
                .returnType(runtimeExceptionType.getType())
                .parameters(new Parameter(null, "cause", "cause", throwableKlass.getType()))
                .build();

        MethodBuilder.newBuilder(runtimeExceptionType, name, code)
                .isConstructor(true)
                .isNative(true)
                .returnType(runtimeExceptionType.getType())
                .parameters(
                        new Parameter(null, "message", "message", Types.getStringType()),
                        new Parameter(null, "cause", "cause", throwableKlass.getType())
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
                default -> new DirectDef<>(javaType, typeDef);
            };
            defMap.preAddDef(def);
            return def;
        }


    }

}
