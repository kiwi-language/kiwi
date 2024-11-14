package org.metavm.entity;

import org.metavm.api.ChildList;
import org.metavm.api.ValueList;
import org.metavm.api.entity.MvObject;
import org.metavm.entity.natives.StandardStaticMethods;
import org.metavm.entity.natives.StdFunction;
import org.metavm.flow.MethodBuilder;
import org.metavm.flow.Nodes;
import org.metavm.flow.Parameter;
import org.metavm.object.instance.ColumnKind;
import org.metavm.object.instance.core.NullValue;
import org.metavm.object.type.*;
import org.metavm.util.*;

import java.io.*;
import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static org.metavm.util.ReflectionUtils.ENUM_NAME_FIELD;
import static org.metavm.util.ReflectionUtils.ENUM_ORDINAL_FIELD;

public class StandardDefBuilder {

    private ValueDef<Enum<?>> enumDef;

    private FieldDef enumNameDef;

    private FieldDef enumOrdinalDef;

    private final SystemDefContext defContext;

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

    private Klass comparatorKlass;

    private final PrimTypeFactory primTypeFactory = new PrimTypeFactory();

    public StandardDefBuilder(SystemDefContext defContext) {
        this.defContext = defContext;
    }

    public void initRootTypes() {
        ValueDef<Record> recordDef = createValueDef(
                Record.class,
                Record.class,
                newKlassBuilder(Record.class)
                        .source(ClassSource.BUILTIN)
                        .kind(ClassKind.VALUE).build(),
                defContext
        );
        defContext.addDef(recordDef);

        var entityKlass = newKlassBuilder(Entity.class)
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
        var enumKlass = newKlassBuilder(Enum.class)
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
                        ColumnKind.STRING.getColumn(0), 0, enumKlass),
                enumDef
        );

        enumOrdinalDef = createFieldDef(
                ENUM_ORDINAL_FIELD,
                createField(ENUM_ORDINAL_FIELD, false, Types.getLongType(), Access.PRIVATE,
                        ColumnKind.INT.getColumn(0), 1, enumKlass),
                enumDef
        );
        enumKlass.setTitleField(enumNameDef.getField());
        createEnumMethods(enumKlass);
        enumKlass.setStage(ResolutionStage.DEFINITION);

        var enumTypeParamDef = new TypeVariableDef(Enum.class.getTypeParameters()[0], enumTypeParam);
        defContext.preAddDef(enumTypeParamDef);
        defContext.addDef(enumDef);
        defContext.afterDefInitialized(enumTypeParamDef);

        initSystemFunctions();

        throwableKlass = newKlassBuilder(Throwable.class)
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
                createField(javaMessageField, false,
                        Types.getNullableType(Types.getStringType()), Access.PUBLIC,
                        ColumnKind.STRING.getColumn(0), 0, throwableKlass),
                throwableDef
        );

        var javaCauseField = ReflectionUtils.getField(Throwable.class, "cause");
        createFieldDef(
                javaCauseField,
                createField(javaCauseField, false,
                        Types.getNullableType(throwableKlass.getType()), Access.PUBLIC,
                        ColumnKind.REFERENCE.getColumn(0), 1, throwableKlass),
                throwableDef
        );
        defContext.afterDefInitialized(throwableDef);
        var exceptionKlass = createExceptionKlass(Exception.class, throwableKlass);
        var ioExceptionKlass = createExceptionKlass(IOException.class, exceptionKlass);
        var objStreamExceptionKlass = createExceptionKlass(ObjectStreamException.class, ioExceptionKlass);
        createExceptionKlass(InvalidObjectException.class, objStreamExceptionKlass);
        var reflectiveOpExceptionKlass = createExceptionKlass(ReflectiveOperationException.class, exceptionKlass);
        createExceptionKlass(ClassNotFoundException.class, reflectiveOpExceptionKlass);
        var runtimeExceptionKlass = createExceptionKlass(RuntimeException.class, exceptionKlass);
        createExceptionKlass(IllegalArgumentException.class, runtimeExceptionKlass);
        createExceptionKlass(IllegalStateException.class, runtimeExceptionKlass);
        createExceptionKlass(NullPointerException.class, runtimeExceptionKlass);
        createExceptionKlass(UnsupportedOperationException.class, runtimeExceptionKlass);
        createExceptionKlass(ConcurrentModificationException.class, runtimeExceptionKlass);
        createExceptionKlass(ClassCastException.class, runtimeExceptionKlass);
        createExceptionKlass(NoSuchElementException.class, runtimeExceptionKlass);
        var indexOutOfBoundExceptiopnKlass = createExceptionKlass(IndexOutOfBoundsException.class, runtimeExceptionKlass);
        createExceptionKlass(CloneNotSupportedException.class, runtimeExceptionKlass);
        var arrayIndexOutOfBoundsExceptionKlass =
                createExceptionKlass(ArrayIndexOutOfBoundsException.class, indexOutOfBoundExceptiopnKlass);
        createArrayIndexOutOfBoundsExceptionFlows(arrayIndexOutOfBoundsExceptionKlass);
        var errorKlass = createExceptionKlass(Error.class, throwableKlass);
        var vmErrorKlass = createExceptionKlass(VirtualMachineError.class, errorKlass);
        createExceptionKlass(InternalError.class, vmErrorKlass);
        createExceptionKlass(OutOfMemoryError.class, vmErrorKlass);
        createMvObjectKlass();

        consumerKlass = createConsumerKlass();
        predicateKlass = createPredicateKlass();
        iteratorKlass = createIteratorKlass();
        iterableKlass = createIterableKlass();
        comparatorKlass = createComparatorKlass();
        createComparableKlass();
        collectionKlass = createCollectionKlass();
        iteratorImplKlass = createIteratorImplKlass();
        setKlass = createSetKlass();
        listKlass = createListKlass();
        mapKlass = createMapKlass();
        createReadWriteListKlass();
        createChildListKlass();
        createValueListKlass();
        createHashSetKlass();
        createTreeSetKlass();
        createHashMapKlass();
        createStringBuilderKlass();
        parseKlass(InputStream.class);
        parseKlass(OutputStream.class);

        primTypeFactory.saveDefs(defContext);
    }

    void postProcess() {
        createObjectInputStream();
        createObjectOutputStream();
        primTypeFactory.saveDefs(defContext);
    }

    private Klass createExceptionKlass(Class<?> javaClass, Klass superKlass) {
        var klass = newKlassBuilder(javaClass)
                .superType(superKlass.getType())
                .source(ClassSource.BUILTIN)
                .build();
        createExceptionFlows(klass);
        defContext.addDef(new DirectDef<>(javaClass, klass));
        return klass;
    }

    private void createEnumMethods(Klass enumKlass) {
        MethodBuilder.newBuilder(enumKlass, "name", "name")
                .isNative(true)
                .returnType(Types.getStringType())
                .build();
        MethodBuilder.newBuilder(enumKlass, "ordinal", "ordinal")
                .isNative(true)
                .returnType(Types.getLongType())
                .build();
    }

    private Klass createConsumerKlass() {
        return parseKlass(Consumer.class);
    }

    private Klass createPredicateKlass() {
        return parseKlass(Predicate.class);
    }

    private Klass createComparableKlass() {
        return parseKlass(Comparable.class);
    }

    private Klass createComparatorKlass() {
        return parseKlass(Comparator.class);
    }

    private Klass createObjectInputStream() {
        return parseKlass(ObjectInputStream.class);
    }

    private Klass createObjectOutputStream() {
        return parseKlass(ObjectOutputStream.class);
    }

    private void initSystemFunctions() {
        StdFunction.defineSystemFunctions().forEach(defContext::writeEntity);
        StandardStaticMethods.defineFunctions().forEach(defContext::writeEntity);
    }

    public void initUserFunctions() {
        StdFunction.defineUserFunctions(defContext).forEach(defContext::writeEntity);
    }

    @SuppressWarnings("SameParameterValue")
    private <T extends Entity> EntityDef<T> createEntityDef(java.lang.reflect.Type javaType,
                                                            Class<T> javaClass,
                                                            Klass type,
                                                            SystemDefContext defContext) {
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
                                           SystemDefContext defContext) {
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
                                                     int tag,
                                                     Klass declaringType) {
        return FieldBuilder.newBuilder(
                        EntityUtils.getMetaFieldName(javaField),
                        javaField.getName(),
                        declaringType, type)
                .asTitle(asTitle)
                .column(column)
                .tag(tag)
                .access(access)
                .defaultValue(new NullValue(Types.getNullType()))
                .staticValue(new NullValue(Types.getNullType()))
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
        return parseKlass(Iterator.class);
    }

    public Klass createIterableKlass() {
        return parseKlass(Iterable.class);
    }

    public Klass createCollectionKlass() {
        return parseKlass(Collection.class);
    }

    public Klass createSetKlass() {
        return parseKlass(Set.class);
    }

    public Klass createListKlass() {
        return parseKlass(List.class);
    }

    public Klass createReadWriteListKlass() {
        return createListImplKlass(ArrayList.class, ClassKind.CLASS, ArrayKind.READ_WRITE);
    }

    public Klass createChildListKlass() {
        return createListImplKlass(ChildList.class, ClassKind.CLASS, ArrayKind.CHILD);
    }

    public Klass createValueListKlass() {
        return createListImplKlass(ValueList.class, ClassKind.VALUE, ArrayKind.VALUE);
    }

    public Klass createStringBuilderKlass() {
        var klass = parseKlass(StringBuilder.class);
        klass.setEphemeral(true);
        FieldBuilder.newBuilder("array", "array", klass,
                new ArrayType(Types.getNullableAnyType(), ArrayKind.READ_WRITE))
                .isChild(true)
                .build();
        MethodBuilder.newBuilder(klass, "isEmpty", "isEmpty")
                .returnType(Types.getBooleanType())
                .isNative(true)
                .build();
        return klass;
    }

    public Klass createListImplKlass(Class<?> javaClass, ClassKind kind, ArrayKind arrayKind) {
        var listImplType = parseKlass(javaClass);
        var elementType = listImplType.getTypeParameters().get(0);
        FieldBuilder.newBuilder("array", "array", listImplType,
                        new ArrayType(Types.getNullableType(elementType.getType()), arrayKind))
                .access(Access.PRIVATE)
                .isChild(kind != ClassKind.VALUE)
                .build();
        return listImplType;
    }

    public Klass createHashSetKlass() {
        var klass = parseKlass(HashSet.class);
        var elementType = klass.getTypeParameters().get(0);
        FieldBuilder.newBuilder("array", "array", klass,
                        new ArrayType(Types.getNullableType(elementType.getType()), ArrayKind.READ_WRITE))
                .access(Access.PRIVATE)
                .isChild(true)
                .build();
        return klass;
    }

    private Klass createTreeSetKlass() {
        var klass = parseKlass(TreeSet.class);
        var elementType = klass.getTypeParameters().get(0);
        FieldBuilder.newBuilder("array", "array", klass,
                        new ArrayType(elementType.getType(), ArrayKind.READ_WRITE))
                .access(Access.PRIVATE)
                .isChild(true)
                .build();
        return klass;
    }

    public Klass createIteratorImplKlass() {
        return parseKlass(IteratorImpl.class);
    }

    public Klass createMapKlass() {
        return parseKlass(Map.class);
    }

    private Klass createHashMapKlass() {
        return createMapImplKlass(HashMap.class, ClassKind.CLASS, ArrayKind.READ_WRITE);
    }

    private Klass createMapImplKlass(Class<?> javaClass, ClassKind kind, ArrayKind valueArrayKind) {
        var mapImplKlass = parseKlass(javaClass);
        var keyTypeVar = mapImplKlass.getTypeParameters().get(0);
        var valueTypeVar = mapImplKlass.getTypeParameters().get(1);

        FieldBuilder.newBuilder("keyArray", "keyArray", mapImplKlass,
                        new ArrayType(Types.getNullableType(keyTypeVar.getType()), ArrayKind.READ_WRITE))
                .access(Access.PRIVATE)
                .isChild(kind != ClassKind.VALUE)
                .build();
        FieldBuilder.newBuilder("valueArray", "valueArray", mapImplKlass,
                        new ArrayType(Types.getNullableType(valueTypeVar.getType()), valueArrayKind))
                .access(Access.PRIVATE)
                .isChild(kind != ClassKind.VALUE)
                .build();
        return mapImplKlass;
    }

    private Klass createMvObjectKlass() {
        var klass = newKlassBuilder(MvObject.class)
                .source(ClassSource.BUILTIN)
                .build();
        var c = MethodBuilder.newBuilder(klass, "MvObject", "MvObject")
                .isConstructor(true)
                .build();
        var scope = c.getScope();
        Nodes.this_(scope);
        Nodes.ret(scope);
        defContext.addDef(new DirectDef<>(MvObject.class, klass));
        klass.emitCode();
        return klass;
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
        createExceptionFlows(exceptionType.getName(), exceptionType.getName(), exceptionType);
    }

    private void createArrayIndexOutOfBoundsExceptionFlows(Klass klass) {
        MethodBuilder.newBuilder(klass, klass.getName(), klass.getName())
                .isConstructor(true)
                .isNative(true)
                .parameters(Parameter.create("index", Types.getLongType()))
                .returnType(klass.getType())
                .build();
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
                .parameters(new Parameter(null, "message", "message", Types.getNullableStringType()))
                .build();

        MethodBuilder.newBuilder(runtimeExceptionType, name, code)
                .isConstructor(true)
                .isNative(true)
                .returnType(runtimeExceptionType.getType())
                .parameters(new Parameter(null, "cause", "cause", Types.getNullableType(throwableKlass.getType())))
                .build();

        MethodBuilder.newBuilder(runtimeExceptionType, name, code)
                .isConstructor(true)
                .isNative(true)
                .returnType(runtimeExceptionType.getType())
                .parameters(
                        new Parameter(null, "message", "message", Types.getNullableStringType()),
                        new Parameter(null, "cause", "cause", Types.getNullableType(throwableKlass.getType()))
                )
                .build();
    }

    public KlassBuilder newKlassBuilder(Class<?> javaClass) {
        return KlassBuilder.newBuilder(javaClass.getSimpleName(), javaClass.getName())
                .tag(defContext.getTypeTag(javaClass));
    }

    Klass parseKlass(Class<?> javaClass) {
        if(javaClass == Enum.class)
            return enumDef.getKlass();
        if(javaClass == Throwable.class)
            return throwableKlass;
        if(javaClass == Class.class)
            return defContext.getKlass(Klass.class);
        var klass = (Klass) primTypeFactory.javaType2TypeDef.get(javaClass);
        if(klass == null) {
            var r = new ReflectDefiner(javaClass, defContext.getTypeTag(javaClass), this::parseKlass,
                    primTypeFactory::putType).defineClass();
            klass = r.klass();
            for (int i = 0; i < javaClass.getTypeParameters().length; i++) {
                primTypeFactory.putType(javaClass.getTypeParameters()[i], klass.getTypeParameters().get(i));
            }
            if(r.staticFieldTable() != null)
                primTypeFactory.putStaticFieldTable(klass, r.staticFieldTable());
        }
        return klass;
    }

    private static class PrimTypeFactory extends TypeFactory {

        private final Map<java.lang.reflect.Type, TypeDef> javaType2TypeDef = new HashMap<>();
        private final Map<TypeDef, java.lang.reflect.Type> typeDef2JavaType = new IdentityHashMap<>();
        private final Map<TypeDef, StaticFieldTable> staticFieldTableMap = new HashMap<>();

        @Override
        public void putType(java.lang.reflect.Type javaType, TypeDef typeDef) {
            NncUtils.requireFalse(javaType2TypeDef.containsKey(javaType));
            NncUtils.requireFalse(typeDef2JavaType.containsKey(typeDef));
            javaType2TypeDef.put(javaType, typeDef);
            typeDef2JavaType.put(typeDef, javaType);
        }

        public void putStaticFieldTable(TypeDef typeDef, StaticFieldTable staticFieldTable) {
            staticFieldTableMap.put(typeDef, staticFieldTable);
        }

        public Map<java.lang.reflect.Type, TypeDef> getMap() {
            return Collections.unmodifiableMap(javaType2TypeDef);
        }

        public void saveDefs(SystemDefContext defContext) {
            var newTypeDefs = NncUtils.exclude(javaType2TypeDef.values(), defContext::containsDef);
            for (var typeDef : newTypeDefs) {
                createDefIfAbsent(typeDef, defContext);
            }
            for (var typeDef : newTypeDefs) {
                defContext.afterDefInitialized(defContext.getDef(typeDef));
            }
        }

        private ModelDef<?> createDefIfAbsent(TypeDef typeDef, SystemDefContext defContext) {
            var javaType = NncUtils.requireNonNull(typeDef2JavaType.get(typeDef));
            var sft = staticFieldTableMap.get(typeDef);
            var def = switch (typeDef) {
                case TypeVariable typeVariable -> new TypeVariableDef(
                        (java.lang.reflect.TypeVariable<?>) javaType, typeVariable
                );
                default -> new DirectDef<>(javaType, typeDef, sft);
            };
            defContext.preAddDef(def);
            return def;
        }


    }

}
