package tech.metavm.util;

import javassist.util.proxy.ProxyObject;
import tech.metavm.entity.EntityMethodHandler;
import tech.metavm.entity.IInstanceContext;
import tech.metavm.object.instance.*;
import tech.metavm.object.instance.persistence.TimePO;
import tech.metavm.object.meta.AnyType;
import tech.metavm.object.meta.PrimitiveKind;
import tech.metavm.object.meta.PrimitiveType;
import tech.metavm.object.meta.Type;

import java.util.*;
import java.util.function.Predicate;

public class InstanceUtils {

//    private static volatile Function<Class<?>, Type> getTypeFunc = ModelDefRegistry::getType;

//    public static void setGetTypeFunc(Function<Class<?>, Type> getTypeFunc) {
//        getTypeFunc = getTypeFunc;
//    }

//    public static void resetGetTypeFunc() {
//        getTypeFunc = ModelDefRegistry::getType;
//    }

    public static final Map<Class<?>, Type> JAVA_CLASS_TO_BASIC_TYPE = Map.of(
            Integer.class, new PrimitiveType(PrimitiveKind.INT),
            Long.class, new PrimitiveType(PrimitiveKind.LONG),
            Double.class, new PrimitiveType(PrimitiveKind.DOUBLE),
            Boolean.class, new PrimitiveType(PrimitiveKind.BOOLEAN),
            String.class, new PrimitiveType(PrimitiveKind.STRING),
            Date.class, new PrimitiveType(PrimitiveKind.TIME),
            Password.class, new PrimitiveType(PrimitiveKind.PASSWORD),
            Null.class, new PrimitiveType(PrimitiveKind.NULL),
            Object.class, new AnyType()
    );

    public static final Map<Type, Class<?>> BASIC_TYPE_JAVA_CLASS;

    public static final Map<Class<?>, Class<?>> JAVA_CLASS_TO_INSTANCE_CLASS = Map.of(
            Integer.class, IntInstance.class,
            Long.class, LongInstance.class,
            Double.class, DoubleInstance.class,
            Boolean.class, BooleanInstance.class,
            String.class, StringInstance.class,
            Date.class, TimeInstance.class,
            Password.class, PasswordInstance.class,
            Null.class, NullInstance.class,
            Object.class, Instance.class
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

    public static LongInstance convertToLong(IntInstance intInstance) {
        return createLong((long) intInstance.getValue());
    }

    public static boolean isAllIntegers(Instance instance1, Instance instance2) {
        return isInteger(instance1) && isInteger(instance2);
    }

    private static boolean isInteger(Instance instance) {
        return instance instanceof IntInstance || instance instanceof LongInstance;
    }

    public static boolean isAnyNull(Instance...instances) {
        for (Instance instance : instances) {
            if(instance instanceof NullInstance) {
                return true;
            }
        }
        return false;
    }

    public static Instance resolveValue(Type fieldType, Object columnValue) {
        if(columnValue instanceof Instance instance) {
            return instance;
        }
        NncUtils.requireTrue(fieldType.isPrimitive(),
                "Can not resolve value '" + columnValue + "' for type " + fieldType);
        return resolvePrimitiveValue(fieldType, columnValue);
    }

    public static PrimitiveInstance resolvePrimitiveValue(Type fieldType, Object columnValue) {
        if(columnValue == null) {
            return InstanceUtils.nullInstance();
        }
        if(columnValue instanceof TimePO timePO) {
            return InstanceUtils.timeInstance(timePO.time());
        }
        if(ValueUtil.isInteger(columnValue)) {
            if(fieldType.isInt()) {
                return intInstance(((Number) columnValue).intValue());
            }
            else if(fieldType.isDouble()) {
                return doubleInstance(((Number) columnValue).doubleValue());
            }
            else if(fieldType.isTime()) {
                return timeInstance(((Number) columnValue).longValue());
            }
            else {
                return longInstance(((Number) columnValue).longValue());
            }
        }
        else if(ValueUtil.isFloat(columnValue)) {
            return doubleInstance(((Number) columnValue).doubleValue());
        }
        else if(columnValue instanceof Boolean bool) {
            return booleanInstance(bool);
        }
        else if(columnValue instanceof String str) {
            if(fieldType.isPassword()) {
                return passwordInstance(str);
            }
            return stringInstance(str);
        }
        else if(columnValue instanceof Password password) {
            return passwordInstance(password.getPassword());
        }
        throw new InternalException("Can not resolve column value '" + columnValue + "' for type " + fieldType);
    }

    public static PrimitiveInstance primitiveInstance(Object value) {
        if(value == null) {
            return nullInstance();
        }
        if(value instanceof Integer i) {
            return intInstance(i);
        }
        if(value instanceof Long l) {
            return longInstance(l);
        }
        if(value instanceof Double d) {
            return doubleInstance(d);
        }
        if(value instanceof Boolean b) {
            return booleanInstance(b);
        }
        if(value instanceof String s) {
            return stringInstance(s);
        }
        if(value instanceof Date date) {
            return timeInstance(date.getTime());
        }
        if(value instanceof Password password) {
            return passwordInstance(password.getPassword());
        }
        throw new InternalException("Value '" + value + "' is not a primitive value");
    }

    public static PrimitiveInstance intInstance(int value) {
        return new IntInstance(
                value, getIntType()
        );
    }

    public static LongInstance longInstance(long value) {
        return new LongInstance(
                value, getLongType()
        );
    }

    public static BooleanInstance booleanInstance(boolean value) {
        return new BooleanInstance(
                value, getBooleanType()
        );
    }

    public static DoubleInstance doubleInstance(double value) {
        return new DoubleInstance(
                value, getDoubleType()
        );
    }

    public static TimeInstance timeInstance(long value) {
        return new TimeInstance(
                value, getTimeType()
        );
    }

    public static NullInstance nullInstance() {
        return new NullInstance(getNullType());
    }

    public static BooleanInstance trueInstance() {
        return booleanInstance(true);
    }

    public static BooleanInstance falseInstance() {
        return booleanInstance(false);
    }

    public static PasswordInstance passwordInstance(String password) {
        return new PasswordInstance(
                password, getPasswordType()
        );
    }

    public static StringInstance stringInstance(String value) {
        return new StringInstance(
                value, getStringType()
        );
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
        if(newInstances.isEmpty()) {
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
        return a.isGreaterThanOrEqualTo(b).getValue() ? a : b;
    }

    public static IntInstance max(IntInstance a, IntInstance b) {
        return a.isGreaterThanOrEqualTo(b).getValue() ? a : b;
    }

    public static DoubleInstance max(DoubleInstance a, DoubleInstance b) {
        return a.isGreaterThanOrEqualTo(b).getValue() ? a : b;
    }

    public static LongInstance min(LongInstance a, LongInstance b) {
        return a.isLessThanOrEqualTo(b).getValue() ? a : b;
    }

    public static IntInstance min(IntInstance a, IntInstance b) {
        return a.isLessThanOrEqualTo(b).getValue() ? a : b;
    }

    public static DoubleInstance min(DoubleInstance a, DoubleInstance b) {
        return a.isLessThanOrEqualTo(b).getValue() ? a : b;
    }

    public static boolean isInitialized(Instance instance) {
        if(instance instanceof ProxyObject proxyObject) {
            EntityMethodHandler<?> handler = (EntityMethodHandler<?>) proxyObject.getHandler();
            return handler.isInitialized();
        }
        else {
            return true;
        }
    }

    public static StringInstance createString(String value) {
        return new StringInstance(value, getStringType());
    }

    public static IntInstance createInt(int value) {
        return new IntInstance(value, getIntType());
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
        return new BooleanInstance(b, getBooleanType());
    }

    public static PrimitiveType getStringType() {
        return getPrimitiveType(String.class);
    }

    public static PrimitiveType getIntType() {
        return getPrimitiveType(Integer.class);
    }

    public static PrimitiveType getLongType() {
        return getPrimitiveType(Long.class);
    }

    public static PrimitiveType getBooleanType() {
        return getPrimitiveType(Boolean.class);
    }

    public static PrimitiveType getDoubleType() {
        return getPrimitiveType(Double.class);
    }

    public static PrimitiveType getTimeType() {
        return getPrimitiveType(Date.class);
    }

    public static PrimitiveType getPasswordType() {
        return getPrimitiveType(Password.class);
    }

    public static PrimitiveType getNullType() {
        return getPrimitiveType(Null.class);
    }

    public static AnyType getAnyType() {
        return new AnyType();
    }

    private static ArrayType getAnyArrayType() {
        return new ArrayType(getAnyType());
    }

    public static ArrayInstance createArray() {
        return createArray(List.of());
    }

    public static ArrayInstance createArray(List<Instance> instances) {
        return new ArrayInstance(getAnyArrayType(), instances);
    }

    public static PrimitiveType getPrimitiveType(Class<?> javaClass) {
        return NncUtils.cast(
                PrimitiveType.class,
                getBasicType(javaClass),
                "Can not get a primitive type for java class '" + javaClass + "'. "
        );
    }

    public static Type getBasicType(Class<?> javaClass) {
        javaClass = ReflectUtils.getBoxedClass(javaClass);
        return NncUtils.requireNonNull(
                JAVA_CLASS_TO_BASIC_TYPE.get(javaClass),
                "Can not find a basic type for java class '" + javaClass.getName() + "'"
        );
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

    public static DoubleInstance sum(DoubleInstance a, DoubleInstance b) {
        return a.add(b);
    }

    public static IntInstance sum(IntInstance a, IntInstance b) {
        return a.add(b);
    }

    public static LongInstance sum(LongInstance a, LongInstance b) {
        return a.add(b);
    }

//    public static PrimitiveType getPrimitiveType(Class<?> klass) {
//        return (PrimitiveType) getTypeFunc.apply(klass);
//    }

}
