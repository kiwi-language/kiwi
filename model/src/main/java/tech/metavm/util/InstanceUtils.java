package tech.metavm.util;

import tech.metavm.entity.ModelDefRegistry;
import tech.metavm.entity.StandardTypes;
import tech.metavm.object.instance.core.*;
import tech.metavm.object.type.*;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

public class InstanceUtils {

//    private static volatile Function<Class<?>, Type> getTypeFunc = ModelDefRegistry::getType;

//    public static void setGetTypeFunc(Function<Class<?>, Type> getTypeFunc) {
//        getTypeFunc = getTypeFunc;
//    }

//    public static void resetGetTypeFunc() {
//        getTypeFunc = ModelDefRegistry::getType;
//    }

    public static NullInstance nullInstance;
    public static BooleanInstance trueInstance;
    public static BooleanInstance falseInstance;

    public static final Map<Class<?>, Type> JAVA_CLASS_TO_BASIC_TYPE = Map.of(
            Integer.class, new PrimitiveType(PrimitiveKind.LONG),
            Long.class, new PrimitiveType(PrimitiveKind.LONG),
            Double.class, new PrimitiveType(PrimitiveKind.DOUBLE),
            Boolean.class, new PrimitiveType(PrimitiveKind.BOOLEAN),
            String.class, new PrimitiveType(PrimitiveKind.STRING),
            Date.class, new PrimitiveType(PrimitiveKind.TIME),
            Password.class, new PrimitiveType(PrimitiveKind.PASSWORD),
            Null.class, new PrimitiveType(PrimitiveKind.NULL),
            Object.class, new ObjectType()
    );

    public static final Map<Type, Class<?>> BASIC_TYPE_JAVA_CLASS;

    public static final Map<Class<?>, Class<?>> JAVA_CLASS_TO_INSTANCE_CLASS = Map.of(
            Integer.class, LongInstance.class,
            Long.class, LongInstance.class,
            Double.class, DoubleInstance.class,
            Boolean.class, BooleanInstance.class,
            String.class, StringInstance.class,
            Date.class, TimeInstance.class,
            Password.class, PasswordInstance.class,
            Null.class, NullInstance.class,
            Object.class, Instance.class,
            Table.class, ArrayInstance.class
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

    public static BooleanInstance equals(Instance first, Instance second) {
        return createBoolean(Objects.equals(first, second));
    }

    public static BooleanInstance notEquals(Instance first, Instance second) {
        return createBoolean(!Objects.equals(first, second));
    }

    public static boolean isAllTime(Instance instance1, Instance instance2) {
        return instance1 instanceof TimeInstance || instance2 instanceof TimeInstance;
    }

    public static boolean isAllIntegers(Instance instance1, Instance instance2) {
        return isInteger(instance1) && isInteger(instance2);
    }

    public static boolean isAllNumbers(Instance instance1, Instance instance2) {
        return isNumber(instance1) && isNumber(instance2);
    }

    public static boolean isNumber(Instance instance) {
        return isInteger(instance) || instance instanceof DoubleInstance;
    }

    private static boolean isInteger(Instance instance) {
        return instance instanceof LongInstance;
    }

    public static boolean isAnyNull(Instance... instances) {
        for (Instance instance : instances) {
            if (instance instanceof NullInstance) {
                return true;
            }
        }
        return false;
    }

    public static PrimitiveInstance serializePrimitive(Object value, Function<Class<?>, Type> getTypeFunc) {
        return NncUtils.requireNonNull(trySerializePrimitive(value, getTypeFunc),
                () -> new InternalException(String.format("Can not resolve primitive value '%s", value)));
    }

    public static boolean isPrimitive(Object value) {
        return value == null || value instanceof Date || value instanceof String ||
                value instanceof Boolean || value instanceof Password ||
                ValueUtil.isInteger(value) || ValueUtil.isFloat(value);
    }

    public static @Nullable PrimitiveInstance trySerializePrimitive(Object value, Function<Class<?>, Type> getTypeFunc) {
        if (value == null)
            return InstanceUtils.nullInstance();
        if (ValueUtil.isInteger(value))
            return InstanceUtils.longInstance(((Number) value).longValue(), getTypeFunc);
        if (ValueUtil.isFloat(value))
            return InstanceUtils.doubleInstance(((Number) value).doubleValue(), getTypeFunc);
        if (value instanceof Boolean bool)
            return InstanceUtils.booleanInstance(bool, getTypeFunc);
        if (value instanceof String str)
            return InstanceUtils.stringInstance(str, getTypeFunc);
        if (value instanceof Password password)
            return InstanceUtils.passwordInstance(password.getPassword(), getTypeFunc);
        if (value instanceof Date date)
            return InstanceUtils.timeInstance(date.getTime(), getTypeFunc);
        return null;
    }

    @SuppressWarnings("unchecked")
    public static <T> T deserializePrimitive(PrimitiveInstance instance, Class<T> javaClass) {
        javaClass = (Class<T>) ReflectUtils.getBoxedClass(javaClass);
        if (instance.isNull()) {
            return null;
        }
        if (instance instanceof LongInstance longInstance) {
            if (javaClass == int.class || javaClass == Integer.class)
                return (T) Integer.valueOf(longInstance.getValue().intValue());
            else if (javaClass == short.class || javaClass == Short.class)
                return (T) Short.valueOf(longInstance.getValue().shortValue());
            else if (javaClass == byte.class || javaClass == Byte.class)
                return (T) Byte.valueOf(longInstance.getValue().byteValue());
            else
                return javaClass.cast(longInstance.getValue());
        }
        if (instance instanceof DoubleInstance doubleInstance) {
            if (javaClass == float.class || javaClass == Float.class)
                return (T) Float.valueOf(doubleInstance.getValue().floatValue());
            else if (javaClass == double.class || javaClass == Double.class)
                return javaClass.cast(doubleInstance.getValue());
        }
        if (instance instanceof PasswordInstance passwordInstance) {
            return javaClass.cast(new Password(passwordInstance));
        }
        if (instance instanceof TimeInstance timeInstance) {
            return javaClass.cast(new Date(timeInstance.getValue()));
        }
        return javaClass.cast(instance.getValue());
    }

    public static PrimitiveInstance primitiveInstance(Object value) {
        if (value == null) {
            return nullInstance();
        }
        if (value instanceof Integer i) {
            return longInstance(i);
        }
        if (value instanceof Long l) {
            return longInstance(l);
        }
        if (value instanceof Double d) {
            return doubleInstance(d);
        }
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

    public static LongInstance longInstance(long value) {
        return longInstance(value, defaultGetTypeFunc());
    }

    public static LongInstance longInstance(long value, Function<Class<?>, Type> getTypeFunc) {
        return new LongInstance(value, getLongType(getTypeFunc));
    }

    public static BooleanInstance booleanInstance(boolean value) {
        return value ? trueInstance : falseInstance;
    }

    public static BooleanInstance booleanInstance(boolean value, Function<Class<?>, Type> getTypeFunc) {
        return new BooleanInstance(value, getBooleanType(getTypeFunc));
    }

    public static DoubleInstance doubleInstance(double value) {
        return new DoubleInstance(value, StandardTypes.getDoubleType());
    }

    public static DoubleInstance doubleInstance(double value, Function<Class<?>, Type> getTypeFunc) {
        return new DoubleInstance(value, getDoubleType(getTypeFunc));
    }

    public static TimeInstance timeInstance(long value, Function<Class<?>, Type> getTypeFunc) {
        return new TimeInstance(value, getTimeType(getTypeFunc));
    }

    public static TimeInstance timeInstance(long value) {
        return new TimeInstance(value, StandardTypes.getTimeType());
    }

    public static NullInstance nullInstance() {
        return nullInstance;
    }

    public static void setNullInstance(NullInstance nullInstance) {
        InstanceUtils.nullInstance = nullInstance;
    }

    public static BooleanInstance trueInstance() {
        return trueInstance;
    }

    public static BooleanInstance falseInstance() {
        return falseInstance;
    }

    public static void setTrueInstance(BooleanInstance trueInstance) {
        InstanceUtils.trueInstance = trueInstance;
    }

    public static void setFalseInstance(BooleanInstance falseInstance) {
        InstanceUtils.falseInstance = falseInstance;
    }

    public static PasswordInstance passwordInstance(String password) {
        return new PasswordInstance(password, StandardTypes.getPasswordType());
    }

    public static PasswordInstance passwordInstance(String password, Function<Class<?>, Type> getTypeFunc) {
        return new PasswordInstance(password, getPasswordType(getTypeFunc));
    }

    public static StringInstance stringInstance(String value) {
        return new StringInstance(value, StandardTypes.getStringType());
    }

    public static StringInstance stringInstance(String value, Function<Class<?>, Type> getTypeFunc) {
        return new StringInstance(value, getStringType(getTypeFunc));
    }

    public static Set<Instance> getAllNewInstances(Instance instance, IInstanceContext context) {
        return getAllNewInstances(List.of(instance), context);
    }

    public static Set<Instance> getAllNewInstances(Collection<Instance> instances, IInstanceContext context) {
        return getAllInstances(
                instances,
                instance -> !context.containsInstance(instance) && !instance.isValue()
        );
    }

    public static Set<Instance> getAllNonValueInstances(Instance root) {
        return getAllNonValueInstances(List.of(root));
    }

    public static Set<Instance> getAllNonValueInstances(Collection<Instance> roots) {
        return getAllInstances(roots, inst -> !inst.isValue());
    }

    public static Set<Instance> getAllInstances(Collection<Instance> roots, Predicate<Instance> filter) {
        IdentitySet<Instance> results = new IdentitySet<>();
        getAllInstances(roots, filter, results);
        return results;
    }

    private static void getAllInstances(Collection<Instance> instances, Predicate<Instance> filter, IdentitySet<Instance> results) {
        List<Instance> newInstances = NncUtils.filter(
                instances, instance -> filter.test(instance) && !results.contains(instance)
        );
        if (newInstances.isEmpty()) {
            return;
        }
        results.addAll(newInstances);
        getAllInstances(
                NncUtils.flatMap(newInstances, Instance::getRefInstances),
                filter,
                results
        );
    }

    public static LongInstance max(LongInstance a, LongInstance b) {
        return a.ge(b).getValue() ? a : b;
    }

    public static DoubleInstance max(DoubleInstance a, DoubleInstance b) {
        return a.ge(b).getValue() ? a : b;
    }

    public static LongInstance min(LongInstance a, LongInstance b) {
        return a.le(b).getValue() ? a : b;
    }

    public static DoubleInstance min(DoubleInstance a, DoubleInstance b) {
        return a.le(b).getValue() ? a : b;
    }

    public static StringInstance createString(String value) {
        return new StringInstance(value, getStringType());
    }

    public static LongInstance createLong(long value) {
        return new LongInstance(value, getLongType());
    }

    public static DoubleInstance createDouble(double value) {
        return new DoubleInstance(value, getDoubleType());
    }

    public static NullInstance createNull() {
        return new NullInstance(getNullType());
    }

    public static BooleanInstance createBoolean(boolean b) {
        return b ? trueInstance : falseInstance;
    }

    public static PrimitiveType getStringType(Function<Class<?>, Type> getTypeFunc) {
        return getPrimitiveType(String.class, getTypeFunc);
    }

    public static PrimitiveType getStringType() {
        return getPrimitiveType(String.class, ModelDefRegistry::getType);
    }

    public static PrimitiveType getIntType(Function<Class<?>, Type> getTypeFunc) {
        return getPrimitiveType(Integer.class, getTypeFunc);
    }

    public static PrimitiveType getIntType() {
        return getPrimitiveType(Integer.class);
    }

    public static PrimitiveType getLongType(Function<Class<?>, Type> getTypeFunc) {
        return getPrimitiveType(Long.class, getTypeFunc);
    }

    public static PrimitiveType getLongType() {
        return getPrimitiveType(Long.class);
    }

    public static PrimitiveType getBooleanType(Function<Class<?>, Type> getTypeFunc) {
        return getPrimitiveType(Boolean.class, getTypeFunc);
    }

    public static PrimitiveType getBooleanType() {
        return getPrimitiveType(Boolean.class);
    }

    public static PrimitiveType getDoubleType() {
        return getDoubleType(defaultGetTypeFunc());
    }

    public static PrimitiveType getDoubleType(Function<Class<?>, Type> getTypeFunc) {
        return getPrimitiveType(Double.class, getTypeFunc);
    }

    public static PrimitiveType getTimeType() {
        return getPrimitiveType(Date.class);
    }

    public static PrimitiveType getTimeType(Function<Class<?>, Type> getTypeFunc) {
        return getPrimitiveType(Date.class, getTypeFunc);
    }

    public static PrimitiveType getPasswordType() {
        return getPrimitiveType(Password.class);
    }

    public static PrimitiveType getPasswordType(Function<Class<?>, Type> getTypeFunc) {
        return getPrimitiveType(Password.class, getTypeFunc);
    }

    public static PrimitiveType getNullType() {
        return getPrimitiveType(Null.class);
    }

    public static PrimitiveType getNullType(Function<Class<?>, Type> getTypeFunc) {
        return getPrimitiveType(Null.class, getTypeFunc);
    }

    public static ObjectType getAnyType() {
        return new ObjectType();
    }

    public static ObjectType getAnyType(Function<Class<?>, Type> getTypeFunc) {
        return (ObjectType) getTypeFunc.apply(Object.class);
    }

    private static ArrayType getObjectArrayType() {
        return ModelDefRegistry.getDefContext().getArrayType(StandardTypes.getObjectType(), ArrayKind.READ_WRITE);
    }

    public static ArrayInstance createArray() {
        return createArray(List.of());
    }

    public static ArrayInstance createArray(List<Instance> instances) {
        return new ArrayInstance(getObjectArrayType(), instances);
    }

    public static PrimitiveType getPrimitiveType(Class<?> javaClass) {
        return getPrimitiveType(javaClass, defaultGetTypeFunc());
    }

    public static PrimitiveType getPrimitiveType(Class<?> javaClass, Function<Class<?>, Type> getTypeFunc) {
        return NncUtils.cast(
                PrimitiveType.class,
                getBasicType(javaClass, getTypeFunc),
                "Can not get a primitive type for java class '" + javaClass + "'. "
        );
    }

    public static Type getBasicType(Class<?> javaClass) {
        return getBasicType(javaClass, defaultGetTypeFunc());
    }

    public static Type getBasicType(Class<?> javaClass, Function<Class<?>, Type> getTypeFunc) {
        javaClass = ReflectUtils.getBoxedClass(javaClass);
        return NncUtils.requireNonNull(
                getTypeFunc.apply(javaClass),
                "Can not find a basic type for java class '" + javaClass.getName() + "'"
        );
    }

    private static Function<Class<?>, Type> defaultGetTypeFunc() {
//        return JAVA_CLASS_TO_BASIC_TYPE::get;
        return ModelDefRegistry::getType;
    }

    public static Class<?> getJavaClassByBasicType(Type type) {
        return NncUtils.requireNonNull(
                BASIC_TYPE_JAVA_CLASS.get(type),
                "Type '" + type + "' is not a basic type"
        );
    }

    public static Class<?> getInstanceClassByJavaClass(Class<?> javaClass) {
        return NncUtils.requireNonNull(
                JAVA_CLASS_TO_INSTANCE_CLASS.get(javaClass),
                "Can not find instance class for java class '" + javaClass.getName() + "'"
        );
    }

    public static Class<?> getJavaClassByInstanceClass(Class<?> instanceClass) {
        return NncUtils.requireNonNull(
                INSTANCE_CLASS_TO_JAVA_CLASS.get(instanceClass),
                "Can not find java class for instance class '" + instanceClass.getName() + "'"
        );
    }

    public static Type getTypeByInstanceClass(Class<?> instanceClass) {
        return getBasicType(getJavaClassByInstanceClass(instanceClass));
    }

    public static boolean isTrue(Instance instance) {
        return (instance instanceof BooleanInstance booleanInstance) && booleanInstance.isTrue();
    }

    public static boolean isFalse(Instance instance) {
        return (instance instanceof BooleanInstance booleanInstance) && booleanInstance.isFalse();
    }

    public static DoubleInstance sum(DoubleInstance a, DoubleInstance b) {
        return a.add(b);
    }

    public static LongInstance sum(LongInstance a, LongInstance b) {
        return a.add(b);
    }

//    public static PrimitiveType getPrimitiveType(Class<?> klass) {
//        return (PrimitiveType) getTypeFunc.apply(klass);
//    }

}
