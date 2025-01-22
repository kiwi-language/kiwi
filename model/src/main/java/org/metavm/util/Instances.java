package org.metavm.util;

import lombok.extern.slf4j.Slf4j;
import org.metavm.api.ReadonlyList;
import org.metavm.beans.BeanDefinitionRegistry;
import org.metavm.ddl.Commit;
import org.metavm.ddl.FieldChange;
import org.metavm.entity.*;
import org.metavm.entity.natives.CallContext;
import org.metavm.entity.natives.IterableNative;
import org.metavm.entity.natives.ListNative;
import org.metavm.entity.natives.NativeMethods;
import org.metavm.flow.Flows;
import org.metavm.flow.Method;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.instance.core.*;
import org.metavm.object.instance.persistence.InstancePO;
import org.metavm.object.type.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

@Slf4j
public class Instances {

    private static final Logger logger = LoggerFactory.getLogger(Instances.class);

    public static final Map<Class<?>, Type> JAVA_CLASS_TO_BASIC_TYPE = Map.ofEntries(
            Map.entry(Integer.class, PrimitiveType.intType),
            Map.entry(Short.class, PrimitiveType.shortType),
            Map.entry(Byte.class, PrimitiveType.byteType),
            Map.entry(Long.class, PrimitiveType.longType),
            Map.entry(Character.class, PrimitiveType.charType),
            Map.entry(Double.class, PrimitiveType.doubleType),
            Map.entry(Float.class, PrimitiveType.floatType),
            Map.entry(Boolean.class, PrimitiveType.booleanType),
            Map.entry(String.class, PrimitiveType.stringType),
            Map.entry(Date.class, PrimitiveType.timeType),
            Map.entry(Password.class, PrimitiveType.passwordType),
            Map.entry(Null.class, NullType.instance),
            Map.entry(Object.class, AnyType.instance)
    );

    public static final Map<Type, Class<?>> BASIC_TYPE_JAVA_CLASS;

    public static final Map<Class<?>, Class<?>> JAVA_CLASS_TO_INSTANCE_CLASS = Map.ofEntries(
            Map.entry(Integer.class, IntValue.class),
            Map.entry(Long.class, LongValue.class),
            Map.entry(Double.class, DoubleValue.class),
            Map.entry(Float.class, FloatValue.class),
            Map.entry(Boolean.class, BooleanValue.class),
            Map.entry(String.class, StringValue.class),
            Map.entry(Date.class, TimeValue.class),
            Map.entry(Character.class, CharValue.class),
            Map.entry(Short.class, ShortValue.class),
            Map.entry(Byte.class, ByteValue.class),
            Map.entry(Password.class, PasswordValue.class),
            Map.entry(Null.class, NullValue.class),
            Map.entry(Object.class, Value.class),
            Map.entry(ReadonlyList.class, ArrayInstance.class)
    );

    private static final Map<Class<?>, Class<?>> INSTANCE_CLASS_TO_JAVA_CLASS;

    static {
        Map<Type, Class<?>> map = new HashMap<>();
        JAVA_CLASS_TO_BASIC_TYPE.forEach((javaClass, basicType) -> map.put(basicType, javaClass));
        BASIC_TYPE_JAVA_CLASS = Collections.unmodifiableMap(map);

        Map<Class<?>, Class<?>> classMap = new HashMap<>();
        JAVA_CLASS_TO_INSTANCE_CLASS.forEach((javaClass, instanceClass) -> classMap.put(instanceClass, javaClass));
        INSTANCE_CLASS_TO_JAVA_CLASS = Collections.unmodifiableMap(classMap);
    }

    public static <T extends Reference> List<T> sort(List<T> instances, boolean desc) {
        if (desc)
            instances.sort((i1, i2) -> Utils.compareId(i2.tryGetTreeId(), i1.tryGetTreeId()));
        else
            instances.sort((i1, i2) -> Utils.compareId(i1.tryGetTreeId(), i2.tryGetTreeId()));
        return instances;
    }

    public static <T extends Reference> List<T> sortAndLimit(List<T> instances, boolean desc, long limit) {
        sort(instances, desc);
        if (limit == -1L)
            return instances;
        else
            return instances.subList(0, Math.min(instances.size(), (int) limit));
    }

    public static int compare(Instance instance1, Instance instance2) {
        if (instance1.isIdInitialized() && instance2.isIdInitialized())
            return instance1.getId().compareTo(instance2.getId());
        else
            return Integer.compare(instance1.getSeq(), instance2.getSeq());
    }

    public static boolean isAnyNull(Value... instances) {
        for (Value instance : instances) {
            if (instance instanceof NullValue) {
                return true;
            }
        }
        return false;
    }

    public static ArrayInstance arrayInstance(ArrayType type, List<? extends Value> elements) {
        return new ArrayInstance(type, elements);
    }

    public static Reference arrayValue(List<? extends Value> elements) {
        return arrayInstance(getAnyArrayType(), elements).getReference();
    }

    public static ClassInstance classInstance(Klass type, Map<Field, Value> fields) {
        return new MvClassInstance(null, fields, type);
    }

    public static Value serializePrimitive(Object value) {
        return Objects.requireNonNull(trySerializePrimitive(value),
                () -> String.format("Can not resolve primitive value '%s", value));
    }

    public static boolean isPrimitive(Object value) {
        return value == null || value instanceof Date || value instanceof String ||
                value instanceof Boolean || value instanceof Password ||
                ValueUtils.isInteger(value) || ValueUtils.isFloatOrDouble(value);
    }

    public static @Nullable Value trySerializePrimitive(Object value) {
        if (value == null)
            return Instances.nullInstance();
        if (ValueUtils.isLong(value))
            return Instances.longInstance(((Number) value).longValue());
        if (ValueUtils.isInteger(value))
            return Instances.intInstance(((Number) value).intValue());
        if (ValueUtils.isDouble(value))
            return Instances.doubleInstance(((Number) value).doubleValue());
        if (ValueUtils.isFloat(value))
            return Instances.floatInstance(((Number) value).floatValue());
        if (value instanceof Boolean bool)
            return Instances.booleanInstance(bool);
        if (value instanceof String str)
            return Instances.stringInstance(str);
        if (value instanceof Password password)
            return Instances.passwordInstance(password.getPassword());
        if (value instanceof Date date)
            return Instances.timeInstance(date.getTime());
        return null;
    }

    public static String getInstanceDetailedDesc(Value instance) {
        if (instance instanceof Reference r && r.get() instanceof ClassInstance clsInst && clsInst.getInstanceType().isList()) {
            var listNative = new ListNative(clsInst);
            var array = listNative.toArray();
            return clsInst.getInstanceType().getName() + " [" + Utils.join(array, Instances::getInstanceDesc) + "]";
        } else
            return getInstanceDesc(instance);
    }

    public static String getInstanceDesc(Instance instance) {
        if (instance instanceof Entity entity)
            return EntityUtils.getEntityDesc(entity);
        else
            return instance.toString();
    }

    public static String getInstanceDesc(Value instance) {
        if (instance instanceof Reference r) {
            if (r.get() instanceof Entity entity)
                return EntityUtils.getEntityDesc(entity);
            else
                return r.get().toString();
        } else
            return instance.toString();
    }

    public static String getInstancePath(Instance instance) {
        if (instance instanceof Entity entity)
            return EntityUtils.getEntityPath(entity);
        else {
            var path = new LinkedList<Instance>();
            var i = instance;
            while (i != null) {
                path.addFirst(i);
                i = i.getParent();
            }
            return Utils.join(path, Instances::getInstanceDesc, "->");
        }
    }

    public static String getInstancePath(Value instance) {
        if (instance instanceof Reference r) {
            if (r.get() instanceof Entity entity)
                return EntityUtils.getEntityPath(entity);
            else {
                var path = new LinkedList<Reference>();
                var i = r.get();
                while (i != null) {
                    path.addFirst(i.getReference());
                    i = i.getParent();
                }
                return Utils.join(path, Instances::getInstanceDesc, "->");
            }
        } else
            return getInstanceDesc(instance);
    }

    @SuppressWarnings("unchecked")
    public static <T> T deserializePrimitive(PrimitiveValue instance, Class<T> javaClass) {
        javaClass = (Class<T>) ReflectionUtils.getWrapperClass(javaClass);
        switch (instance) {
            case IntValue intValue -> {
                if (javaClass == short.class || javaClass == Short.class)
                    return (T) Short.valueOf(intValue.getValue().shortValue());
                else if (javaClass == byte.class || javaClass == Byte.class)
                    return (T) Byte.valueOf(intValue.getValue().byteValue());
                else
                    return javaClass.cast(intValue.getValue());
            }
            case LongValue longInstance -> {
                return javaClass.cast(longInstance.getValue());
            }
            case DoubleValue doubleInstance -> {
                return javaClass.cast(doubleInstance.getValue());
            }
            case FloatValue floatInstance -> {
                return javaClass.cast(floatInstance.getValue());
            }
            case PasswordValue passwordInstance -> {
                return javaClass.cast(new Password(passwordInstance));
            }
            case TimeValue timeInstance -> {
                return javaClass.cast(new Date(timeInstance.getValue()));
            }
            default -> {
                return javaClass.cast(instance.getValue());
            }
        }
    }

    public static Value primitiveInstance(Object value) {
        if (value == null) {
            return nullInstance();
        }
        if (value instanceof Integer i) {
            return intInstance(i);
        }
        if (value instanceof Long l) {
            return longInstance(l);
        }
        if (value instanceof Double d) {
            return doubleInstance(d);
        }
        if (value instanceof Float f)
            return floatInstance(f);
        if (value instanceof Boolean b) {
            return booleanInstance(b);
        }
        if (value instanceof String s) {
            return stringInstance(s);
        }
        if (value instanceof Date date) {
            return timeInstance(date.getTime());
        }
        if (value instanceof Password password) {
            return passwordInstance(password.getPassword());
        }
        throw new InternalException("Value '" + value + "' is not a primitive value");
    }

    public static LongValue longInstance(long value) {
        return new LongValue(value);
    }

    public static IntValue intOne() {
        return intInstance(1);
    }

    public static LongValue longOne() {
        return longInstance(1L);
    }

    public static LongValue longZero() {
        return longInstance(0L);
    }

    public static CharValue charInstance(char value) {
        return new CharValue(value);
    }

    public static BooleanValue booleanInstance(boolean value) {
        return new BooleanValue(value);
    }

    public static IntValue intInstance(boolean value) {
        return value ? IntValue.one : IntValue.zero;
    }

    public static DoubleValue doubleInstance(double value) {
        return new DoubleValue(value);
    }

    public static TimeValue timeInstance(long value) {
        return new TimeValue(value);
    }

    public static NullValue nullInstance() {
        return NullValue.instance;
    }

    public static BooleanValue trueInstance() {
        return new BooleanValue(true);
    }

    public static BooleanValue falseInstance() {
        return new BooleanValue(false);
    }

    public static IntValue one() {
        return IntValue.one;
    }

    public static IntValue zero() {
        return IntValue.zero;
    }

    public static PasswordValue passwordInstance(String password) {
        return new PasswordValue(password);
    }

    public static StringValue stringInstance(String value) {
        return new StringValue(value);
    }

    public static LongValue max(LongValue a, LongValue b) {
        return a.ge(b).getValue() ? a : b;
    }

    public static DoubleValue max(DoubleValue a, DoubleValue b) {
        return a.ge(b).getValue() ? a : b;
    }

    public static LongValue min(LongValue a, LongValue b) {
        return a.le(b).getValue() ? a : b;
    }

    public static DoubleValue min(DoubleValue a, DoubleValue b) {
        return a.le(b).getValue() ? a : b;
    }

    public static StringValue createString(String value) {
        return new StringValue(value);
    }

    public static BooleanValue createBoolean(boolean b) {
        return b ? trueInstance() : falseInstance();
    }

    private static ArrayType getAnyArrayType() {
        return new ArrayType(AnyType.instance, ArrayKind.READ_WRITE);
    }

    public static ArrayInstance createArray(List<Value> instances) {
        return new ArrayInstance(getAnyArrayType(), instances);
    }

    public static Type getBasicType(Class<?> javaClass) {
        return getBasicType(javaClass, defaultGetTypeFunc());
    }

    public static Type getBasicType(Class<?> javaClass, Function<Class<?>, Type> getTypeFunc) {
        javaClass = ReflectionUtils.getWrapperClass(javaClass);
        if (javaClass == Byte.class)
            return Types.getByteType();
        if (javaClass == Short.class)
            return Types.getShortType();
        if (javaClass == Integer.class)
            return Types.getIntType();
        if (javaClass == Long.class)
            return Types.getLongType();
        if (javaClass == Float.class)
            return Types.getFloatType();
        if (javaClass == Double.class)
            return Types.getDoubleType();
        if (javaClass == char.class || javaClass == Character.class)
            return Types.getCharType();
        if (javaClass == Boolean.class)
            return Types.getBooleanType();
        if (javaClass == String.class)
            return Types.getStringType();
        if (javaClass == Date.class)
            return Types.getTimeType();
        if (javaClass == Password.class)
            return Types.getPasswordType();
        if (javaClass == Null.class)
            return Types.getNullType();
        if (javaClass == Object.class)
            return Types.getAnyType();
        if (javaClass == Table.class)
            return Types.getAnyArrayType();
        if (javaClass == ReadonlyList.class)
            return Types.getReadOnlyAnyArrayType();
        return Objects.requireNonNull(
                getTypeFunc.apply(javaClass),
                "Can not find a basic type for java class '" + javaClass.getName() + "'"
        );
    }

    private static Function<Class<?>, Type> defaultGetTypeFunc() {
//        return JAVA_CLASS_TO_BASIC_TYPE::get;
        return ModelDefRegistry::getType;
    }

    public static Class<?> getJavaClassByBasicType(Type type) {
        return Objects.requireNonNull(
                BASIC_TYPE_JAVA_CLASS.get(type),
                "Type '" + type + "' is not a basic type"
        );
    }

    public static Class<?> getInstanceClassByJavaClass(Class<?> javaClass) {
        return Objects.requireNonNull(
                JAVA_CLASS_TO_INSTANCE_CLASS.get(javaClass),
                "Can not find instance class for java class '" + javaClass.getName() + "'"
        );
    }

    public static Class<?> getJavaClassByInstanceClass(Class<?> instanceClass) {
        return Objects.requireNonNull(
                INSTANCE_CLASS_TO_JAVA_CLASS.get(instanceClass),
                "Can not find java class for instance class '" + instanceClass.getName() + "'"
        );
    }

    public static Type getTypeByInstanceClass(Class<?> instanceClass) {
        return getBasicType(getJavaClassByInstanceClass(instanceClass));
    }

    public static boolean isTrue(Value instance) {
        return (instance instanceof BooleanValue booleanInstance) && booleanInstance.isTrue();
    }

    public static boolean isFalse(Value instance) {
        return (instance instanceof BooleanValue booleanInstance) && booleanInstance.isFalse();
    }

    public static DoubleValue sum(DoubleValue a, DoubleValue b) {
        return a.add(b);
    }

    public static LongValue sum(LongValue a, LongValue b) {
        return a.add(b);
    }

    public static List<Reference> merge(List<Reference> result1, List<Reference> result2, boolean desc, long limit) {
        return sortAndLimit(new ArrayList<>(Utils.mergeUnique(result1, result2)), desc, limit);
    }

    public static ClassInstance createList(ClassType listType, List<? extends Value> elements) {
        if (listType.isList()) {
            var elementType = listType.getFirstTypeArgument();
            if (listType.getKlass() == StdKlass.list.get()) {
                listType = KlassType.create(StdKlass.arrayList.get(), List.of(elementType));
            }
            var list = ClassInstance.allocate(listType);
            var listNative = new ListNative(list);
            listNative.List();
            for (Value element : elements) {
                listNative.add(element);
            }
            return list;
        } else
            throw new IllegalArgumentException(listType + " is not a List type");
    }

    public static void applyDDL(Iterable<? extends Instance> instances, Commit commit, IInstanceContext context) {
        var newFields = Utils.map(commit.getNewFieldIds(), context::getField);
        var convertingFields = Utils.map(commit.getConvertingFieldIds(), context::getField);
        var toChildFields = Utils.map(commit.getToChildFieldIds(), context::getField);
        var changingSuperKlasses = Utils.map(commit.getChangingSuperKlassIds(), context::getKlass);
        var toValueKlasses = Utils.map(commit.getEntityToValueKlassIds(), context::getKlass);
        var valueToEntityKlasses = Utils.map(commit.getValueToEntityKlassIds(), context::getKlass);
        var toEnumKlasses = Utils.map(commit.getToEnumKlassIds(), context::getKlass);
        var removingChildFields = Utils.map(commit.getRemovedChildFieldIds(), context::getField);
        var runMethods = Utils.map(commit.getRunMethodIds(), context::getMethod);
        var newIndexes = Utils.map(commit.getNewIndexIds(), id -> context.getEntity(Index.class, id));
        var searchEnabledKlasses = Utils.map(commit.getSearchEnabledKlassIds(), context::getKlass);
        for (Instance instance : instances) {
            if (instance instanceof MvInstance mvInst && !mvInst.isEnum()) {
                applyDDL(mvInst, newFields, convertingFields, toChildFields, changingSuperKlasses, toValueKlasses,
                        valueToEntityKlasses, toEnumKlasses, removingChildFields, runMethods, newIndexes, searchEnabledKlasses,
                        commit, context);
            }
        }
    }

    public static void applyDDL(MvInstance instance,
                                Collection<Field> newFields,
                                Collection<Field> convertingFields,
                                Collection<Field> toChildFields,
                                Collection<Klass> changingSuperKlasses,
                                Collection<Klass> toValueKlasses,
                                Collection<Klass> valueToEntityKlasses,
                                Collection<Klass> toEnumKlasses,
                                Collection<Field> removingChildFields,
                                Collection<Method> runMethods,
                                Collection<Index> newIndexes,
                                Collection<Klass> searchEnabledKlasses,
                                @Nullable RedirectStatus redirectStatus,
                                IInstanceContext context) {
        if (instance instanceof ClassInstance clsInst) {
            for (Field field : newFields) {
                var k = clsInst.getInstanceType().asSuper(field.getDeclaringType());
                if (k != null) {
                    initializeField(clsInst, field, context);
                }
            }
            for (Field field : convertingFields) {
                var k = clsInst.getInstanceType().asSuper(field.getDeclaringType());
                if (k != null) {
                    convertField(clsInst, field, context);
                }
            }
            for (Field field : toChildFields) {
                var k = clsInst.getInstanceType().asSuper(field.getDeclaringType());
                if (k != null) {
                    var value = clsInst.getField(field);
                    if (value instanceof Reference r)
                        r.get().setParent(clsInst, field);
                }
            }
            for (Klass klass : changingSuperKlasses) {
                var k = clsInst.getInstanceType().asSuper(klass);
                if (k != null)
                    initializeSuper(clsInst, klass, context);
            }
            for (Klass klass : toValueKlasses) {
                handleEntityToValueConversion((MvClassInstance) clsInst, klass);
            }
            for (Field removingChildField : removingChildFields) {
                var k = clsInst.getInstanceType().asSuper(removingChildField.getDeclaringType());
                if (k == null)
                    continue;
                var f = k.getField(removingChildField).getRawField();
                var childRef = clsInst.getField(f);
                if (childRef.isNull())
                    continue;
                var child = childRef.resolveMv();
                var ref = new Object() {
                    boolean referenced;
                };
                var r = child.getReference();
                child.getRoot().forEachDescendant(i -> {
                    ((MvInstance) i).forEachReference((ii, isChild) -> {
                        if (!isChild && ii.equals(r))
                            ref.referenced = true;
                    });
                });
                if (!ref.referenced) {
                    var referring = context.getByReferenceTargetId(child.getId(), 0, 1);
                    if (!referring.isEmpty())
                        ref.referenced = true;
                }
                if (ref.referenced)
                    throw new InternalException(
                            "Unable to delete child field " +
                                    f.getQualifiedName()
                                    + " because the child object " + child + " is still referenced by other objects"
                    );
                child.setRemoving();
            }
            for (Method runMethod : runMethods) {
                var k = clsInst.getInstanceType().asSuper(runMethod.getDeclaringType());
                if (k != null) {
                    var pm = clsInst.getInstanceType().getMethod(runMethod);
                    Flows.invoke(pm, clsInst, List.of(), context);
                }
            }
            for (var index : newIndexes) {
                var k = clsInst.getInstanceType().asSuper(index.getDeclaringType());
                if (k != null) {
                    context.forceReindex(clsInst);
                }
            }
            for (Klass klass : searchEnabledKlasses) {
                var k = clsInst.getInstanceType().asSuper(klass);
                if (k != null) {
                    context.forceSearchReindex(clsInst);
                }
            }
        }
        for (Klass klass : valueToEntityKlasses) {
            instance.forEachReference(r -> {
                if (r.isResolved()) {
                    var resolved = r.get();
                    if (resolved instanceof ClassInstance clsInst) {
                        var k = clsInst.getInstanceType().asSuper(klass);
                        if (k != null)
                            r.setEager();
                    }
                }
            });
        }
        for (Klass klass : toEnumKlasses) {
            handleEnumConversion(instance, klass, redirectStatus, context);
        }
    }

    private static void handleEntityToValueConversion(MvClassInstance instance, Klass klass) {
        instance.forEachReference((r, isChild, type) -> {
            if (type.isAssignableFrom(klass.getType())) {
                var referent = r.get();
                if (referent instanceof ClassInstance object && object.getInstanceType().asSuper(klass) != null)
                    r.setEager();
            }
        });
    }

    private static void handleEnumConversion(MvInstance instance, Klass enumClass, @Nullable RedirectStatus redirectStatus, IInstanceContext context) {
        instance.transformReference((r, isChild, type) -> {
            if (type.isAssignableFrom(enumClass.getType())) {
                var referent = r.get();
                var sft = StaticFieldTable.getInstance(enumClass.getType(), context);
                if (referent instanceof ClassInstance object && object.getInstanceKlass() == enumClass && !sft.isEnumConstant(object.getReference())) {
                    var r1 = object.getReference();
                    object.setField(StdField.enumName.get(), Instances.stringInstance(""));
                    object.setField(StdField.enumOrdinal.get(), Instances.intInstance(-1));
                    var ec = mapEnumConstant(r1, enumClass, context);
                    return redirectStatus != null ? new RedirectingReference(referent, ec, redirectStatus) : ec;
                }
            }
            return r;
        });
    }

    private static Reference mapEnumConstant(Reference instance, Klass enumClass, IInstanceContext context) {
        var mapper = getEnumConstantMapper(enumClass);
        return (Reference) requireNonNull(Flows.invoke(mapper.getRef(), null, List.of(instance), context));
    }

    private static Method getEnumConstantMapper(Klass enumClass) {
        var found = enumClass.findMethod(m -> m.isStatic() && m.getName().equals("__map__") && m.getParameterTypes().equals(List.of(enumClass.getType())));
        if (found == null)
            throw new IllegalStateException("Failed to find an enum constant mapper in class " + enumClass.getName());
        return found;
    }

    public static void setEagerFlag(List<Instance> referring, Id id) {
        for (Instance ref : referring) {
            ref.forEachReference(r -> {
                if (r.idEquals(id))
                    r.setEager();
            });
        }
    }

    public static void initializeField(ClassInstance instance, Field field, IInstanceContext context) {
        var initialValue = computeFieldInitialValue(instance, field, context);
        instance.setFieldForce(field, initialValue);
    }

    public static @Nullable Method findFieldInitializer(Field field, boolean includeFromEnum) {
        var klass = field.getDeclaringType();
        var initMethodName = "__" + field.getName() + "__";
        var found = klass.findMethodByNameAndParamTypes(initMethodName, List.of());
        if (found != null || !includeFromEnum)
            return found;
        return klass.findMethodByNameAndParamTypes(initMethodName, List.of(Types.getStringType(), Types.getIntType()));
    }

    public static @Nullable Value getDefaultValue(Field field, IInstanceContext context) {
        var type = field.getType();
        if (type.isNullable())
            return Instances.nullInstance();
        else if (type instanceof PrimitiveType primitiveType)
            return primitiveType.getDefaultValue();
        else if (field.getType() instanceof KlassType ct) {
            var beanDefReg = BeanDefinitionRegistry.getInstance(context);
            var beans = beanDefReg.getBeansOfType(ct);
            if (!beans.isEmpty())
                return beans.getFirst().getReference();
        }
        return null;
    }

    public static Value computeFieldInitialValue(ClassInstance instance, Field field, IInstanceContext context) {
        var initMethod = findFieldInitializer(field, true);
        if (initMethod != null) {
            if (initMethod.getParameters().isEmpty())
                return field.getType().fromStackValue(Flows.invoke(initMethod.getRef(), instance, List.of(), context));
            else if (initMethod.getParameterTypes().equals(List.of(Types.getStringType(), Types.getIntType()))) {
                return field.getType().fromStackValue(Flows.invoke(
                        initMethod.getRef(),
                        instance,
                        List.of(
                                instance.getUnknownField(StdKlass.enum_.get().getTag(), StdField.enumName.get().getTag()),
                                instance.getUnknownField(StdKlass.enum_.get().getTag(), StdField.enumOrdinal.get().getTag())
                        ),
                        context
                ));
            } else
                throw new IllegalStateException("Invalid initializer method: " + initMethod.getSignatureString());
        } else if (field.getInitializer() != null)
            return field.getType().fromStackValue(Flows.invoke(field.getInitializer().getRef(), instance, List.of(), context));
        else
            return requireNonNull(getDefaultValue(field, context), "Can not find a default value for field " + field.getQualifiedName());
    }

    public static void convertField(ClassInstance instance, Field field, IInstanceContext context) {
        if (DebugEnv.traceDDL) {
            log.trace("Converting field {} for instance {}, original tag: {}, tag: {}, type: {}",
                    field.getQualifiedName(), instance, field.getOriginalTag(), field.getTag(), field.getType().getTypeDesc());
        }
        var convertedValue = computeConvertedFieldValue(instance, field, context);
        instance.setField(field, convertedValue);
    }

    public static @Nullable Method findTypeConverter(Field field) {
        var klass = field.getDeclaringType();
        var initMethodName = "__" + field.getName() + "__";
        return klass.findMethod(
                m -> initMethodName.equals(m.getName())
                        && m.getParameters().size() == 1
                        && m.getReturnType().equals(field.getType())
        );
    }

    public static Value computeConvertedFieldValue(ClassInstance instance, Field field, IInstanceContext context) {
        var converter = requireNonNull(findTypeConverter(field));
        var originalValue = instance.getUnknownField(field.getDeclaringType().getTag(), field.getOriginalTag());
        return field.getType().fromStackValue(Flows.invoke(converter.getRef(), instance, List.of(originalValue), context));
    }

    private static void initializeSuper(ClassInstance instance, Klass klass, IInstanceContext context) {
        computeSuper(instance, klass, context);
    }


    public static Method findSuperInitializer(Klass klass) {
        var superType = requireNonNull(klass.getSuperType());
        var initMethodName = "__" + superType.getName() + "__";
        return klass.findMethod(
                m -> initMethodName.equals(m.getName())
                        && m.getParameters().isEmpty()
                        && m.getReturnType().equals(superType)
        );
    }

    private static void computeSuper(ClassInstance instance, Klass klass, IInstanceContext context) {
        var superInitializer = findSuperInitializer(klass);
        if (superInitializer != null) {
            var initializer = requireNonNull(superInitializer);
            var s = requireNonNull(Flows.invoke(initializer.getRef(), instance, List.of(), context)).resolveObject();
            s.setEphemeral();
            s.forEachField(instance::setFieldForce);
        } else {
            var superKlass = requireNonNull(klass.getSuperType()).getKlass();
            superKlass.forEachField(field -> {
                if (!instance.isFieldInitialized(field) || instance.getField(field).isNull()) {
                    instance.setFieldForce(field,
                            requireNonNull(Instances.getDefaultValue(field, context),
                                    () -> "Default value is missing for field: " + field.getQualifiedName()));
                }
            });
        }
    }

    public static void rollbackDDL(Iterable<Instance> instances, Commit commit, IInstanceContext context) {
        for (FieldChange fieldChange : commit.getFieldChanges()) {
            var klass = context.getKlass(fieldChange.klassId());
            for (Instance instance : instances) {
                if (instance instanceof ClassInstance object) {
                    var k = object.getInstanceType().asSuper(klass);
                    if (k != null)
                        object.tryClearUnknownField(k.getKlassTag(), fieldChange.newTag());
                }
            }
        }
        for (Instance instance : instances) {
            if(instance instanceof MvInstance mvInst) {
                for (String toChildFieldId : commit.getToChildFieldIds()) {
                    var field = context.getField(toChildFieldId);
                    if (instance instanceof ClassInstance object) {
                        var k = object.getInstanceType().asSuper(field.getDeclaringType());
                        if (k != null) {
                            var value = object.getField(field);
                            if (value instanceof Reference ref)
                                ref.resolveMv().clearParent();
                        }
                    }
                }
                if (mvInst.isRemoving())
                    mvInst.clearRemoving();
            }
        }
        for (List<String> klassIdsList : List.of(commit.getEntityToValueKlassIds(), commit.getValueToEntityKlassIds())) {
            for (String klassId : klassIdsList) {
                var klass = context.getKlass(klassId);
                for (Instance instance : instances) {
                    instance.forEachReference(r -> {
                        if (r.isEager()) {
                            var referent = r.get();
                            if (referent instanceof ClassInstance object && object.getInstanceType().asSuper(klass) != null) {
                                if (object.isValue())
                                    context.remove(object);
                                else
                                    r.clearEager();
                            }
                        }
                    });
                }
            }
        }
        for (String klassId : commit.getToEnumKlassIds()) {
            var klass = context.getKlass(klassId);
            for (Instance instance : instances) {
                if (instance instanceof MvInstance mvInst) {
                    mvInst.transformReference((r, isChild, type) -> {
                        if (r instanceof RedirectingReference && type.isAssignableFrom(klass.getType())) {
                            if (r.get() instanceof ClassInstance object && object.getInstanceKlass() == klass) {
                                object.tryClearUnknownField(StdKlass.enum_.get().getTag(), StdField.enumName.get().getTag());
                                object.tryClearUnknownField(StdKlass.enum_.get().getTag(), StdField.enumOrdinal.get().getTag());
                                return object.getReference();
                            }
                        }
                        return r;
                    });
                }
            }
        }
    }

    public static int compare(Value value1, Value value2, CallContext callContext) {
        if (value1 instanceof IntValue i1 && value2 instanceof IntValue i2)
            return Integer.compare(i1.getValue(), i2.getValue());
        if (value1 instanceof LongValue l1 && value2 instanceof LongValue l2)
            return Long.compare(l1.getValue(), l2.getValue());
        if (value1 instanceof DoubleValue d1 && value2 instanceof DoubleValue d2)
            return Double.compare(d1.getValue(), d2.getValue());
        if (value1 instanceof FloatValue f1 && value2 instanceof FloatValue f2)
            return Float.compare(f1.getValue(), f2.getValue());
        if (value1 instanceof BooleanValue b1 && value2 instanceof BooleanValue v2)
            return Boolean.compare(b1.getValue(), v2.getValue());
        if (value1 instanceof StringValue s1 && value2 instanceof StringValue s2)
            return s1.getValue().compareTo(s2.getValue());
        if (value1 instanceof NullValue && value2 instanceof NullValue)
            return 0;
        if (value1 instanceof TimeValue t1 && value2 instanceof TimeValue t2)
            return Long.compare(t1.getValue(), t2.getValue());
        if (value1 instanceof PasswordValue p1 && value2 instanceof PasswordValue p2)
            return p1.getValue().compareTo(p2.getValue());
        else if (value1 instanceof Reference r1 && value2 instanceof Reference r2)
            return compare(r1.get(), r2.get(), callContext);
        else
            throw new IllegalArgumentException("Cannot compare values " + value1 + " and " + value2);
    }

    public static int compare(Instance instance1, Instance instance2, CallContext callContext) {
        if (instance1 instanceof ClassInstance clsInst1 && instance2 instanceof ClassInstance clsInst2) {
            var comparableType = clsInst1.getInstanceType().asSuper(StdKlass.comparable.get());
            if (comparableType != null && comparableType.getTypeArguments().getFirst().isInstance(clsInst2.getReference())) {
                var compareToMethod = comparableType.getMethod(StdMethod.comparableCompareTo.get());
                var r = (IntValue) requireNonNull(Flows.invokeVirtual(compareToMethod, clsInst1, List.of(clsInst2.getReference()), callContext));
                return r.value;
            }
        }
        throw new InternalException("Cannot compare " + instance1 + " with " + instance2);
    }

    public static int hashCode(Value value, CallContext callContext) {
        if (value instanceof PrimitiveValue primitiveValue)
            return primitiveValue.hashCode();
        else if (value instanceof Reference reference)
            return hashCode(reference.get(), callContext);
        else
            throw new IllegalArgumentException("Cannot get hash code for value: " + value);
    }

    public static int hashCode(Instance instance, CallContext callContext) {
        if (instance instanceof ClassInstance clsInst) {
            var method = clsInst.getInstanceKlass().getHashCodeMethod();
            if (method != null) {
                var ret = Flows.invoke(method.getRef(), clsInst, List.of(), callContext);
                return ((IntValue) requireNonNull(ret)).value;
            }
            if (clsInst.isValue()) {
                var ref = new Object() {
                    int hash;
                };
                clsInst.forEachField((f, v) -> ref.hash = ref.hash * 31 + hashCode(v, callContext));
                return ref.hash;
            }
        } else if (instance instanceof ArrayInstance array && array.isValue()) {
            int h = 0;
            for (Value element : array.getElements())
                h = h * 31 + hashCode(element, callContext);
            return h;
        }
        return instance.hashCode();
    }

    public static boolean equals(Value value1, Value value2, CallContext callContext) {
        if (value1.equals(value2))
            return true;
        else if (value1 instanceof Reference ref1 && value2 instanceof Reference ref2)
            return equals(ref1.get(), ref2.get(), callContext);
        else
            return false;
    }

    public static boolean equals(Instance instance1, Instance instance2, CallContext callContext) {
        if (instance1 == instance2)
            return true;
        if (instance1 instanceof ClassInstance clsInst) {
            var method = clsInst.getInstanceKlass().getEqualsMethod();
            if (method != null) {
                var ret = Flows.invoke(method.getRef(), clsInst, List.of(instance2.getReference()), callContext);
                return ((IntValue) requireNonNull(ret)).value != 0;
            }
            if (clsInst.isValue() && instance2 instanceof ClassInstance clsInst2 && clsInst2.isValue()
                    && clsInst.getInstanceType().equals(clsInst2.getInstanceType())) {
                var ref = new Object() {
                    boolean equals = true;
                };
                clsInst.forEachField((f, v) -> {
                    if (ref.equals && !equals(v, clsInst2.getField(f), callContext)) {
                        ref.equals = false;
                    }
                });
                return ref.equals;
            }
        } else if (instance1 instanceof ArrayInstance array1 && array1.isValue() && instance2 instanceof ArrayInstance array2 && array2.isValue()
                && array1.getInstanceType().equals(array2.getInstanceType()) && array1.size() == array2.size()) {
            int i = 0;
            for (Value element : array1) {
                if (!equals(element, array2.get(i++), callContext))
                    return false;
            }
            return true;
        }
        return false;
    }

    public static String toString(Value value, CallContext callContext) {
        return switch (value) {
            case PrimitiveValue primitiveValue -> primitiveValue.toString();
            case NullValue ignored -> "null";
            case Reference reference -> toString(reference.get(), callContext);
            case null, default -> throw new IllegalArgumentException("Cannot convert value '" + value + "' to string");
        };
    }

    public static String toString(Instance instance, CallContext callContext) {
        if (instance instanceof ClassInstance clsInst) {
            var method = clsInst.getInstanceKlass().getToStringMethod();
            if (method != null) {
                var ret = Flows.invoke(method.getRef(), clsInst, List.of(), callContext);
                return ((StringValue) requireNonNull(ret)).getValue();
            }
        }
        return instance.getInstanceType().getTypeDesc() + "@" + instance.getStringId();
    }

    public static int toInt(@Nullable Value value) {
        return ((IntValue) requireNonNull(value)).value;
    }

    public static boolean toBoolean(@Nullable Value value) {
        return ((IntValue) requireNonNull(value)).value != 0;
    }

    public static void forEach(Value value, Consumer<? super Value> action) {
        var iterable = value.resolveMvObject();
        var nat = (IterableNative) NativeMethods.getNativeObject(iterable);
        nat.forEach(action);
    }

    public static Value getDefaultValue(Type type) {
        Value defaultValue = null;
        if (type.isNullable())
            defaultValue = nullInstance();
        else if (type instanceof PrimitiveType primitiveType)
            defaultValue = primitiveType.getKind().getDefaultValue();
        if (defaultValue == null)
            defaultValue = nullInstance();
//            throw new InternalException("Cannot get default value for type " + type);
        return defaultValue;
    }

    public static void initArray(ArrayInstance array, int[] dims, int dimOffset) {
        var len = dims[dimOffset];
        if (dimOffset == dims.length - 1) {
            var v = getDefaultValue(array.getInstanceType().getElementType());
            for (int i = 0; i < len; i++) {
                array.addElement(v);
            }
        } else {
            for (int i = 0; i < len; i++) {
                var subArray = ArrayInstance.allocate((ArrayType) array.getInstanceType().getElementType().getUnderlyingType());
                array.addElement(subArray.getReference());
                initArray(subArray, dims, dimOffset + 1);
            }
        }
    }

    public static Klass getGeneralClass(Instance instance) {
        if (instance instanceof ClassInstance clsInst)
            return clsInst.getInstanceKlass();
        else if (instance instanceof ArrayInstance array)
            return Types.getGeneralKlass(array.getInstanceType());
        else
            throw new IllegalArgumentException("Cannot get general klass for instance: " + instance);
    }

    public static Value fromConstant(Object value) {
        return fromJavaValue(value, true, () -> {
            throw new IllegalArgumentException("Cannot create a value for " + value);
        });
    }

    public static Value fromJavaValue(Object value, boolean useStackValue, Supplier<Value> defaultSupplier) {
        return switch (value) {
            case Long l -> longInstance(l);
            case Integer i -> intInstance(i);
            case Short s -> intInstance(s);
            case Byte b -> intInstance(b);
            case Double d -> doubleInstance(d);
            case Float f -> floatInstance(f);
            case Character c -> useStackValue ? intInstance(c) : charInstance(c);
            case Boolean b -> useStackValue ? intInstance(b) : booleanInstance(b);
            case String s -> stringInstance(s);
            case Date t -> timeInstance(t.getTime());
            case Object[] array -> arrayInstance(
                    (ArrayType) Types.fromJavaType(array.getClass()).getUnderlyingType(),
                    Utils.map(
                            array, e -> fromJavaValue(e, false, Instances::nullInstance)
                    )
            ).getReference();
            case null -> nullInstance();
            case Value v -> v;
            default -> defaultSupplier.get();
        };
    }

    public static Object toJavaValue(Value value, Class<?> javaType) {
        return switch (value) {
            case IntValue intValue -> {
                var v = intValue.value;
                if (javaType == byte.class || javaType == Byte.class)
                    yield (byte) v;
                if (javaType == short.class || javaType == Short.class)
                    yield (short) v;
                if (javaType == char.class || javaType == Character.class)
                    yield (char) v;
                yield v;
            }
            case PrimitiveValue primitiveValue -> primitiveValue.getValue();
            case Reference reference -> {
                var inst = reference.resolveDurable();
                if (inst instanceof ArrayInstance array) {
                    var elementJavaType = requireNonNull(Types.getPrimitiveJavaType(array.getInstanceType().getElementType().getUnderlyingType()),
                            () -> "Cannot get java type for type " + array.getInstanceType().getElementType().getUnderlyingType());
                    Object[] javaArray = (Object[]) java.lang.reflect.Array.newInstance(elementJavaType, array.size());
                    for (int i = 0; i < javaArray.length; i++) {
                        javaArray[i] = toJavaValue(array.get(i), elementJavaType);
                    }
                    yield javaArray;
                } else
                    throw new IllegalArgumentException("Cannot get java value for object instance");
            }
            case NullValue ignored -> null;
            default -> throw new IllegalArgumentException("Cannot get java value for value " + value);
        };
    }

    public static IntValue intInstance(int i) {
        return new IntValue(i);
    }

    public static FloatValue floatInstance(float v) {
        return new FloatValue(v);
    }

    public static ShortValue shortInstance(short v) {
        return new ShortValue(v);
    }

    public static ByteValue byteInstance(byte v) {
        return new ByteValue(v);
    }

    public static Reference list(Type elementType, Iterable<Value> values) {
        var type = KlassType.create(StdKlass.arrayList.get(), List.of(elementType));
        var list = ClassInstance.allocate(type);
        var nat = new ListNative(list);
        nat.List();
        values.forEach(nat::add);
        return list.getReference();
    }

    public static InstancePO toPO(Instance instance, long appId) {
        return new InstancePO(
                appId,
                instance.getTreeId(),
                InstanceOutput.toBytes(instance),
                instance.getVersion(),
                instance.getSyncVersion(),
                instance.getNextNodeId()
        );
    }


}
