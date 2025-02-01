package org.metavm.entity;

import lombok.extern.slf4j.Slf4j;
import org.metavm.api.ChildList;
import org.metavm.api.Index;
import org.metavm.api.ValueList;
import org.metavm.api.entity.MvObject;
import org.metavm.entity.natives.StandardStaticMethods;
import org.metavm.entity.natives.StdFunction;
import org.metavm.flow.MethodBuilder;
import org.metavm.flow.NameAndType;
import org.metavm.flow.Nodes;
import org.metavm.object.instance.ColumnKind;
import org.metavm.object.instance.core.NullValue;
import org.metavm.object.type.*;
import org.metavm.util.*;

import javax.annotation.Nullable;
import java.io.*;
import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static org.metavm.util.ReflectionUtils.ENUM_NAME_FIELD;
import static org.metavm.util.ReflectionUtils.ENUM_ORDINAL_FIELD;

@Slf4j
public class StandardDefBuilder {

    private Klass enumKlass;
    private Klass throwableKlass;

    private final SystemDefContext defContext;

    private final PrimTypeFactory primTypeFactory = new PrimTypeFactory();

    public StandardDefBuilder(SystemDefContext defContext) {
        this.defContext = defContext;
    }

    public void initRootTypes() {
        var recordDef = createKlassDef(
                Record.class,
                newKlassBuilder(Record.class)
                        .source(ClassSource.BUILTIN)
                        .kind(ClassKind.VALUE).build()
        );
        defContext.addDef(recordDef);

        var entityKlass = newKlassBuilder(Entity.class)
                .source(ClassSource.BUILTIN)
                .build();
        var entityDef = createKlassDef(
                Entity.class,
                entityKlass
        );

        defContext.addDef(entityDef);

        var enumTypeParam = new TypeVariable(null, "EnumType",
                DummyGenericDeclaration.INSTANCE);
        enumKlass = newKlassBuilder(Enum.class)
                .source(ClassSource.BUILTIN)
                .typeParameters(enumTypeParam)
                .build();

        enumTypeParam.setBounds(List.of(enumKlass.getType()));

        var enumDef = createKlassDef(
                new TypeReference<Enum<?>> () {}.getType(),
                enumKlass
        );
        defContext.preAddDef(enumDef);

        throwableKlass = newKlassBuilder(Throwable.class)
                .source(ClassSource.BUILTIN).build();
        var throwableDef = createKlassDef(
                Throwable.class,
                throwableKlass
        );
        defContext.preAddDef(throwableDef);

        createStringKlass();

        var enumNameField = createField(ENUM_NAME_FIELD, true, Types.getStringType(), Access.PUBLIC,
                ColumnKind.STRING.getColumn(0), 0, enumKlass);
        createField(ENUM_ORDINAL_FIELD, false, Types.getIntType(), Access.PRIVATE,
                ColumnKind.INT.getColumn(0), 1, enumKlass);
        enumKlass.setTitleField(enumNameField);
        createEnumMethods(enumKlass);
        enumKlass.setStage(ResolutionStage.DEFINITION);

        defContext.afterDefInitialized(enumDef);

        initSystemFunctions();

        createThrowableFlows(throwableKlass);
        var javaMessageField = ReflectionUtils.getField(Throwable.class, "detailMessage");
        /*
         Predefine composite types because the 'cause' field depends on Throwable | Null
         Do not call createCompositeTypes, it will initialize the throwable type without fields!
         */
        createField(javaMessageField, false,
                Types.getNullableType(Types.getStringType()), Access.PUBLIC,
                ColumnKind.STRING.getColumn(0), 0, throwableKlass);

        var javaCauseField = ReflectionUtils.getField(Throwable.class, "cause");
        createField(javaCauseField, false,
                Types.getNullableType(throwableKlass.getType()), Access.PUBLIC,
                ColumnKind.REFERENCE.getColumn(0), 1, throwableKlass);
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
        createExceptionKlass(ArithmeticException.class, runtimeExceptionKlass);
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

        createConsumerKlass();
        createPredicateKlass();
        createIteratorKlass();
        createIterableKlass();
        createComparatorKlass();
        createComparableKlass();
        createCollectionKlass();
        createIteratorImplKlass();
        createSetKlass();
        createListKlass();
        createMapKlass();
        createReadWriteListKlass();
        createChildListKlass();
        createValueListKlass();
        createHashSetKlass();
        createTreeSetKlass();
        createHashMapKlass();
        createStringBuilderKlass();
        parseKlass(InputStream.class);
        parseKlass(OutputStream.class);
        parseKlass(Number.class);
        var indexKlass = parseKlass(Index.class);
        FieldBuilder.newBuilder("name", indexKlass, Types.getStringType()).build();
        primTypeFactory.saveDefs(defContext);
        createPrimitiveWrapperKlasses();
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
        defContext.addDef(new KlassDef<>(javaClass, klass));
        return klass;
    }

    private void createEnumMethods(Klass enumKlass) {
        MethodBuilder.newBuilder(enumKlass, enumKlass.getName())
                .parameters(
                        new NameAndType("name", Types.getNullableStringType()),
                        new NameAndType("ordinal", Types.getIntType())
                )
                .isConstructor(true)
                .isNative(true)
                .build();
        MethodBuilder.newBuilder(enumKlass, "name")
                .isNative(true)
                .returnType(Types.getStringType())
                .build();
        MethodBuilder.newBuilder(enumKlass, "ordinal")
                .isNative(true)
                .returnType(Types.getIntType())
                .build();
    }

    private void createPrimitiveWrapperKlasses() {
        createPrimitiveKlass(Byte.class, PrimitiveType.byteType);
        createPrimitiveKlass(Short.class, PrimitiveType.shortType);
        createPrimitiveKlass(Integer.class, PrimitiveType.intType);
        createPrimitiveKlass(Long.class, PrimitiveType.longType);
        createPrimitiveKlass(Float.class, PrimitiveType.floatType);
        createPrimitiveKlass(Double.class, PrimitiveType.doubleType);
        createPrimitiveKlass(Character.class, PrimitiveType.charType);
        createPrimitiveKlass(Boolean.class, PrimitiveType.booleanType);
    }

    private Klass createPrimitiveKlass(Class<?> clazz, PrimitiveType primitiveType) {
        var klass = parseKlass(clazz);
        klass.setKind(ClassKind.VALUE);
        klass.setInterfaces(List.of(KlassType.create(defContext.getKlass(Comparable.class), List.of(klass.getType()))));
        FieldBuilder.newBuilder("value", klass, primitiveType).build();
        return klass;
    }

    private Klass createStringKlass() {
        var klass = parseKlass(String.class, k -> {
            StdKlass.string.set(k);
            k.setType(new StringType());
            k.setKind(ClassKind.VALUE);
        });
        MethodBuilder.newBuilder(klass, "writeObject")
                .isNative(true)
                .access(Access.PRIVATE)
                .parameters(new NameAndType("s", Types.getNullableType(defContext.getType(ObjectOutputStream.class))))
                .build();

        MethodBuilder.newBuilder(klass, "readObject")
                .isNative(true)
                .access(Access.PRIVATE)
                .parameters(new NameAndType("s", Types.getNullableType(defContext.getType(ObjectInputStream.class))))
                .build();

        klass.resetHierarchy();
        return klass;
    }

//    private Klass createStringKlass() {
//        var klass = defContext.getKlass(String.class);
//        var stringType = klass.getType();
//        var nullableStringType = Types.getNullableType(stringType);
//        klass.setInterfaces(List.of(KlassType.create(defContext.getKlass(Comparable.class), List.of(klass.getType()))));
//
//        MethodBuilder.newBuilder(klass, "equals")
//                .isNative(true)
//                .parameters(new NameAndType("that", Types.getNullableAnyType()))
//                .returnType(PrimitiveType.booleanType)
//                .build();
//
//        MethodBuilder.newBuilder(klass, "hashCode")
//                .isNative(true)
//                .returnType(PrimitiveType.intType)
//                .build();
//
//        MethodBuilder.newBuilder(klass, "compareTo")
//                .isNative(true)
//                .parameters(new NameAndType("that", nullableStringType))
//                .returnType(PrimitiveType.intType)
//                .build();
//
//        MethodBuilder.newBuilder(klass, "concat")
//                .isNative(true)
//                .parameters(new NameAndType("that", nullableStringType))
//                .returnType(nullableStringType)
//                .build();
//
//        MethodBuilder.newBuilder(klass, "startsWith")
//                .isNative(true)
//                .parameters(new NameAndType("that", nullableStringType))
//                .returnType(PrimitiveType.booleanType)
//                .build();
//
//        MethodBuilder.newBuilder(klass, "endsWith")
//                .isNative(true)
//                .parameters(new NameAndType("that", nullableStringType))
//                .returnType(PrimitiveType.booleanType)
//                .build();
//
//        MethodBuilder.newBuilder(klass, "contains")
//                .isNative(true)
//                .parameters(new NameAndType("that", Types.getNullableType(klass.getType())))
//                .returnType(PrimitiveType.booleanType)
//                .build();
//
//        MethodBuilder.newBuilder(klass, "substring")
//                .isNative(true)
//                .parameters(
//                        new NameAndType("beginIndex", PrimitiveType.intType)
//                )
//                .returnType(nullableStringType)
//                .build();
//
//        MethodBuilder.newBuilder(klass, "substring")
//                .isNative(true)
//                .parameters(
//                        new NameAndType("beginIndex", PrimitiveType.intType),
//                        new NameAndType("endIndex", PrimitiveType.intType)
//                )
//                .returnType(nullableStringType)
//                .build();
//
//        var nullableCharSequenceType = Types.getNullableType(defContext.getType(CharSequence.class));
//        MethodBuilder.newBuilder(klass, "replace")
//                .isNative(true)
//                .parameters(
//                        new NameAndType("target", nullableCharSequenceType),
//                        new NameAndType("replacement", nullableCharSequenceType)
//                        )
//                .returnType(nullableStringType)
//                .build();
//
//        MethodBuilder.newBuilder(klass, "replace")
//                .isNative(true)
//                .parameters(
//                        new NameAndType("target", Types.getCharType()),
//                        new NameAndType("replacement", Types.getCharType())
//                )
//                .returnType(nullableStringType)
//                .build();
//
//        MethodBuilder.newBuilder(klass, "replaceFirst")
//                .isNative(true)
//                .parameters(
//                        new NameAndType("regex", nullableStringType),
//                        new NameAndType("replacement", nullableStringType)
//                )
//                .returnType(nullableStringType)
//                .build();
//
//
//        MethodBuilder.newBuilder(klass, "replaceAll")
//                .isNative(true)
//                .parameters(
//                        new NameAndType("regex", nullableStringType),
//                        new NameAndType("replacement", nullableStringType)
//                )
//                .returnType(nullableStringType)
//                .build();
//
//        MethodBuilder.newBuilder(klass, "writeObject")
//                .isNative(true)
//                .access(Access.PRIVATE)
//                .parameters(new NameAndType("s", Types.getNullableType(defContext.getType(ObjectOutputStream.class))))
//                .build();
//
//        MethodBuilder.newBuilder(klass, "readObject")
//                .isNative(true)
//                .access(Access.PRIVATE)
//                .parameters(new NameAndType("s", Types.getNullableType(defContext.getType(ObjectInputStream.class))))
//                .build();
//
//        return klass;
//    }

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

    private <T> KlassDef<T> createKlassDef(Class<T> javaClass, Klass type) {
        return new KlassDef<>(javaClass, type);
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
                        declaringType, type)
                .asTitle(asTitle)
                .column(column)
                .tag(tag)
                .access(access)
                .defaultValue(new NullValue())
                .staticValue(new NullValue())
                .build();
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
        FieldBuilder.newBuilder("array", klass,
                new ArrayType(Types.getNullableAnyType(), ArrayKind.READ_WRITE))
                .isChild(true)
                .build();
        MethodBuilder.newBuilder(klass, "isEmpty")
                .returnType(Types.getBooleanType())
                .isNative(true)
                .build();
        return klass;
    }

    public Klass createListImplKlass(Class<?> javaClass, ClassKind kind, ArrayKind arrayKind) {
        var listImplType = parseKlass(javaClass);
        var elementType = listImplType.getTypeParameters().getFirst();
        FieldBuilder.newBuilder("array", listImplType,
                        new ArrayType(Types.getNullableType(elementType.getType()), arrayKind))
                .access(Access.PRIVATE)
                .isChild(kind != ClassKind.VALUE)
                .build();
        return listImplType;
    }

    public Klass createHashSetKlass() {
        var klass = parseKlass(HashSet.class);
        var elementType = klass.getTypeParameters().getFirst();
        FieldBuilder.newBuilder("array", klass,
                        new ArrayType(Types.getNullableType(elementType.getType()), ArrayKind.READ_WRITE))
                .access(Access.PRIVATE)
                .isChild(true)
                .build();
        return klass;
    }

    private Klass createTreeSetKlass() {
        var klass = parseKlass(TreeSet.class);
        var elementType = klass.getTypeParameters().getFirst();
        FieldBuilder.newBuilder("array", klass,
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
        var keyTypeVar = mapImplKlass.getTypeParameters().getFirst();
        var valueTypeVar = mapImplKlass.getTypeParameters().get(1);

        FieldBuilder.newBuilder("keyArray", mapImplKlass,
                        new ArrayType(Types.getNullableType(keyTypeVar.getType()), ArrayKind.READ_WRITE))
                .access(Access.PRIVATE)
                .isChild(kind != ClassKind.VALUE)
                .build();
        FieldBuilder.newBuilder("valueArray", mapImplKlass,
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
        var c = MethodBuilder.newBuilder(klass, "MvObject")
                .isConstructor(true)
                .build();
        var code = c.getCode();
        Nodes.this_(code);
        Nodes.ret(code);
        defContext.addDef(new KlassDef<>(MvObject.class, klass));
        klass.emitCode();
        return klass;
    }

    private void createThrowableFlows(Klass throwableType) {
        MethodBuilder.newBuilder(throwableType, "Throwable")
                .isConstructor(true)
                .isNative(true)
                .returnType(throwableType.getType())
                .build();

        MethodBuilder.newBuilder(throwableType, "Throwable")
                .isConstructor(true)
                .isNative(true)
                .returnType(throwableType.getType())
                .parameters(new NameAndType("message", Types.getStringType()))
                .build();

        MethodBuilder.newBuilder(throwableType, "Throwable")
                .isConstructor(true)
                .isNative(true)
                .returnType(throwableType.getType())
                .parameters(new NameAndType("cause", throwableType.getType()))
                .build();

        MethodBuilder.newBuilder(throwableType, "Throwable")
                .isConstructor(true)
                .isNative(true)
                .returnType(throwableType.getType())
                .parameters(
                        new NameAndType("message", Types.getStringType()),
                        new NameAndType("cause", throwableType.getType())
                )
                .build();

        MethodBuilder.newBuilder(throwableType, "getMessage")
                .isNative(true)
                .returnType(Types.getNullableType(Types.getStringType()))
                .build();
    }

    private void createExceptionFlows(Klass exceptionType) {
        createExceptionFlows(exceptionType.getName(), exceptionType);
    }

    private void createArrayIndexOutOfBoundsExceptionFlows(Klass klass) {
        MethodBuilder.newBuilder(klass, klass.getName())
                .isConstructor(true)
                .isNative(true)
                .parameters(new NameAndType("index", Types.getIntType()))
                .returnType(klass.getType())
                .build();
    }

    private void createExceptionFlows(String name, Klass runtimeExceptionType) {
        MethodBuilder.newBuilder(runtimeExceptionType, name)
                .isConstructor(true)
                .isNative(true)
                .returnType(runtimeExceptionType.getType())
                .build();

        MethodBuilder.newBuilder(runtimeExceptionType, name)
                .isConstructor(true)
                .isNative(true)
                .returnType(runtimeExceptionType.getType())
                .parameters(new NameAndType("message", Types.getNullableStringType()))
                .build();

        MethodBuilder.newBuilder(runtimeExceptionType, name)
                .isConstructor(true)
                .isNative(true)
                .returnType(runtimeExceptionType.getType())
                .parameters(new NameAndType("cause", Types.getNullableType(throwableKlass.getType())))
                .build();

        MethodBuilder.newBuilder(runtimeExceptionType, name)
                .isConstructor(true)
                .isNative(true)
                .returnType(runtimeExceptionType.getType())
                .parameters(
                        new NameAndType("message", Types.getNullableStringType()),
                        new NameAndType("cause", Types.getNullableType(throwableKlass.getType()))
                )
                .build();
    }

    public KlassBuilder newKlassBuilder(Class<?> javaClass) {
        return KlassBuilder.newBuilder(javaClass.getSimpleName(), javaClass.getName())
                .tag(defContext.getTypeTag(javaClass));
    }

    Klass parseKlass(Class<?> javaClass) {
        return parseKlass(javaClass, null);
    }

    Klass parseKlass(Class<?> javaClass, @Nullable Consumer<Klass> preprocessor) {
        var tracing = DebugEnv.traceClassDefinition;
        if(javaClass == Enum.class)
            return enumKlass;
        if(javaClass == Throwable.class)
            return throwableKlass;
        if(javaClass == Class.class)
            return defContext.getKlass(Klass.class);
        var klass = (Klass) primTypeFactory.javaType2TypeDef.get(javaClass);
        if(klass == null) {
            var definer = new ReflectDefiner(javaClass, defContext.getTypeTag(javaClass), this::parseKlass,
                    primTypeFactory::putType);
            definer.setPreprocessor(preprocessor);
            var r = definer.defineClass();
            klass = r.klass();
            if(r.staticFieldTable() != null) {
                if (tracing) log.trace("Registering static field table for class '{}'", javaClass.getName());
                primTypeFactory.putStaticFieldTable(klass, r.staticFieldTable());
            }
        }
        return klass;
    }

    private static class PrimTypeFactory extends TypeFactory {

        private final Map<java.lang.reflect.Type, TypeDef> javaType2TypeDef = new HashMap<>();
        private final Map<TypeDef, Class<?>> typeDef2JavaType = new IdentityHashMap<>();
        private final Map<TypeDef, StaticFieldTable> staticFieldTableMap = new HashMap<>();

        @Override
        public void putType(Class<?> javaClass, TypeDef typeDef) {
            Utils.require(!javaType2TypeDef.containsKey(javaClass),
                    () -> "Java class " + javaClass.getName() + " is already added to the type factory");
            Utils.require(!typeDef2JavaType.containsKey(typeDef));
            javaType2TypeDef.put(javaClass, typeDef);
            typeDef2JavaType.put(typeDef, javaClass);
        }

        public void putStaticFieldTable(TypeDef typeDef, StaticFieldTable staticFieldTable) {
            staticFieldTableMap.put(typeDef, staticFieldTable);
        }

        public Map<java.lang.reflect.Type, TypeDef> getMap() {
            return Collections.unmodifiableMap(javaType2TypeDef);
        }

        public void saveDefs(SystemDefContext defContext) {
            var newTypeDefs = Utils.exclude(javaType2TypeDef.values(), defContext::containsDef);
            for (var typeDef : newTypeDefs) {
                createDefIfAbsent(typeDef, defContext);
            }
            for (var typeDef : newTypeDefs) {
                defContext.afterDefInitialized(defContext.getDef(typeDef));
            }
        }

        private KlassDef<?> createDefIfAbsent(TypeDef typeDef, SystemDefContext defContext) {
            var javaType = Objects.requireNonNull(typeDef2JavaType.get(typeDef));
            var sft = staticFieldTableMap.get(typeDef);
            var def = new KlassDef<>(javaType, (Klass) typeDef);
            def.setStaticFieldTable(sft);
            defContext.preAddDef(def);
            return def;
        }


    }

}
