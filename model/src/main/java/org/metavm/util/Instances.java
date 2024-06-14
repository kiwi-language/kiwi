package org.metavm.util;

import org.metavm.entity.*;
import org.metavm.entity.natives.ListNative;
import org.metavm.object.instance.ObjectInstanceMap;
import org.metavm.object.instance.core.StructuralVisitor;
import org.metavm.object.instance.core.*;
import org.metavm.object.type.*;
import org.metavm.object.view.rest.dto.ObjectMappingRefDTO;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

public class Instances {

    public static final Map<Class<?>, Type> JAVA_CLASS_TO_BASIC_TYPE = Map.of(
            Integer.class, new PrimitiveType(PrimitiveKind.LONG),
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

    public static final Map<Class<?>, Class<?>> JAVA_CLASS_TO_INSTANCE_CLASS = Map.ofEntries(
            Map.entry(Integer.class, LongInstance.class),
            Map.entry(Long.class, LongInstance.class),
            Map.entry(Double.class, DoubleInstance.class),
            Map.entry(Boolean.class, BooleanInstance.class),
            Map.entry(String.class, StringInstance.class),
            Map.entry(Date.class, TimeInstance.class),
            Map.entry(Password.class, PasswordInstance.class),
            Map.entry(Null.class, NullInstance.class),
            Map.entry(Object.class, Instance.class),
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

    public static <T extends DurableInstance> List<T> sort(List<T> instances, boolean desc) {
        if (desc)
            instances.sort((i1, i2) -> NncUtils.compareId(i2.tryGetTreeId(), i1.tryGetTreeId()));
        else
            instances.sort((i1, i2) -> NncUtils.compareId(i1.tryGetTreeId(), i2.tryGetTreeId()));
        return instances;
    }

    public static <T extends DurableInstance> List<T> sortAndLimit(List<T> instances, boolean desc, long limit) {
        sort(instances, desc);
        if (limit == -1L)
            return instances;
        else
            return instances.subList(0, Math.min(instances.size(), (int) limit));
    }

    public static int compare(DurableInstance instance1, DurableInstance instance2) {
        if(instance1.isIdInitialized() && instance2.isIdInitialized())
            return instance1.getId().compareTo(instance2.getId());
        else
            return Integer.compare(instance1.getSeq(), instance2.getSeq());
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

    public static ArrayInstance arrayInstance(ArrayType type, List<Instance> elements) {
        return new ArrayInstance(type, elements);
    }

    public static ClassInstance classInstance(Klass type, Map<Field, Instance> fields) {
        return new ClassInstance(null, fields, type);
    }

    public static PrimitiveInstance serializePrimitive(Object value, Function<Class<?>, Type> getTypeFunc) {
        return NncUtils.requireNonNull(trySerializePrimitive(value, getTypeFunc),
                () -> new InternalException(String.format("Can not resolve primitive value '%s", value)));
    }

    public static boolean isPrimitive(Object value) {
        return value == null || value instanceof Date || value instanceof String ||
                value instanceof Boolean || value instanceof Password ||
                ValueUtils.isInteger(value) || ValueUtils.isFloat(value);
    }

    public static @Nullable PrimitiveInstance trySerializePrimitive(Object value, Function<Class<?>, Type> getTypeFunc) {
        if (value == null)
            return Instances.nullInstance();
        if (ValueUtils.isInteger(value))
            return Instances.longInstance(((Number) value).longValue(), getTypeFunc);
        if (ValueUtils.isFloat(value))
            return Instances.doubleInstance(((Number) value).doubleValue(), getTypeFunc);
        if (value instanceof Boolean bool)
            return Instances.booleanInstance(bool, getTypeFunc);
        if (value instanceof String str)
            return Instances.stringInstance(str, getTypeFunc);
        if (value instanceof Password password)
            return Instances.passwordInstance(password.getPassword(), getTypeFunc);
        if (value instanceof Date date)
            return Instances.timeInstance(date.getTime(), getTypeFunc);
        return null;
    }

    public static String getInstanceDetailedDesc(Instance instance) {
        if(instance instanceof ClassInstance clsInst && clsInst.getType().isList()) {
            var listNative = new ListNative(clsInst);
            var array = listNative.toArray();
            return clsInst.getType().getName() + " [" + NncUtils.join(array, Instances::getInstanceDesc) + "]";
        }
        else
            return getInstanceDesc(instance);
    }

    public static String getInstanceDesc(Instance instance) {
        if(instance instanceof DurableInstance d && d.getMappedEntity() != null)
            return EntityUtils.getEntityDesc(d.getMappedEntity());
        else
            return instance.toString();
    }

    public static List<Instance> filterInTree(Instance instance, Predicate<Instance> predicate) {
        List<Instance> result = new ArrayList<>();
        instance.accept(new StructuralVisitor() {
            @Override
            public Void visitInstance(Instance instance) {
                if(predicate.test(instance))
                    result.add(instance);
                return super.visitInstance(instance);
            }
        });
        return result;
    }

    public static String getInstancePath(Instance instance) {
        if (instance instanceof DurableInstance d) {
            if(d.getMappedEntity() != null)
                return EntityUtils.getEntityPath(d.getMappedEntity());
            else {
                var path = new LinkedList<DurableInstance>();
                var i = d;
                while (i != null) {
                    path.addFirst(i);
                    i = i.getParent();
                }
                return NncUtils.join(path, Instances::getInstanceDesc, "->");
            }
        }
        else
            return getInstanceDesc(instance);
    }

    @SuppressWarnings("unchecked")
    public static <T> T deserializePrimitive(PrimitiveInstance instance, Class<T> javaClass) {
        javaClass = (Class<T>) ReflectionUtils.getBoxedClass(javaClass);
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
        return new LongInstance(value, StandardTypes.getLongType());
    }

    public static LongInstance longInstance(long value, Function<Class<?>, Type> getTypeFunc) {
        return new LongInstance(value, StandardTypes.getLongType());
    }

    public static BooleanInstance booleanInstance(boolean value) {
        return new BooleanInstance(value, StandardTypes.getBooleanType());
    }

    public static BooleanInstance booleanInstance(boolean value, Function<Class<?>, Type> getTypeFunc) {
        return new BooleanInstance(value, StandardTypes.getBooleanType());
    }

    public static DoubleInstance doubleInstance(double value) {
        return new DoubleInstance(value, StandardTypes.getDoubleType());
    }

    public static DoubleInstance doubleInstance(double value, Function<Class<?>, Type> getTypeFunc) {
        return new DoubleInstance(value, StandardTypes.getDoubleType());
    }

    public static TimeInstance timeInstance(long value, Function<Class<?>, Type> getTypeFunc) {
        return new TimeInstance(value, StandardTypes.getTimeType());
    }

    public static TimeInstance timeInstance(long value) {
        return new TimeInstance(value, StandardTypes.getTimeType());
    }

    public static NullInstance nullInstance() {
        return new NullInstance(StandardTypes.getNullType());
    }

    public static BooleanInstance trueInstance() {
        return new BooleanInstance(true, StandardTypes.getBooleanType());
    }

    public static BooleanInstance falseInstance() {
        return new BooleanInstance(false, StandardTypes.getBooleanType());
    }

    public static PasswordInstance passwordInstance(String password) {
        return new PasswordInstance(password, StandardTypes.getPasswordType());
    }

    public static PasswordInstance passwordInstance(String password, Function<Class<?>, Type> getTypeFunc) {
        return new PasswordInstance(password, StandardTypes.getPasswordType());
    }

    public static StringInstance stringInstance(String value) {
        return new StringInstance(value, StandardTypes.getStringType());
    }

    public static StringInstance stringInstance(String value, Function<Class<?>, Type> getTypeFunc) {
        return new StringInstance(value, StandardTypes.getStringType());
    }

    public static Set<DurableInstance> getAllNonValueInstances(DurableInstance root) {
        return getAllNonValueInstances(List.of(root));
    }

    public static Set<DurableInstance> getAllNonValueInstances(Collection<DurableInstance> roots) {
        return getAllInstances(roots, inst -> !inst.isValue());
    }

    public static Set<DurableInstance> getAllInstances(Collection<DurableInstance> roots, Predicate<DurableInstance> filter) {
        IdentitySet<DurableInstance> results = new IdentitySet<>();
        getAllInstances(roots, filter, results);
        return results;
    }

    private static void getAllInstances(Collection<DurableInstance> instances, Predicate<DurableInstance> filter, IdentitySet<DurableInstance> results) {
        var newInstances = NncUtils.filter(
                instances, instance -> filter.test(instance) && !results.contains(instance)
        );
        if (newInstances.isEmpty()) {
            return;
        }
        results.addAll(newInstances);
        getAllInstances(
                NncUtils.flatMap(newInstances, DurableInstance::getRefInstances),
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
        return new StringInstance(value, StandardTypes.getStringType());
    }

    public static LongInstance createLong(long value) {
        return new LongInstance(value, StandardTypes.getLongType());
    }

    public static DoubleInstance createDouble(double value) {
        return new DoubleInstance(value, StandardTypes.getDoubleType());
    }

    public static BooleanInstance createBoolean(boolean b) {
        return b ? trueInstance() : falseInstance();
    }

    private static ArrayType getAnyArrayType() {
        return new ArrayType(new AnyType(), ArrayKind.READ_WRITE);
    }

    public static ArrayInstance createArray() {
        return createArray(List.of());
    }

    public static ArrayInstance createArray(List<Instance> instances) {
        return new ArrayInstance(getAnyArrayType(), instances);
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
        javaClass = ReflectionUtils.getBoxedClass(javaClass);
        if (javaClass == Long.class || javaClass == Integer.class)
            return StandardTypes.getLongType();
        if (javaClass == Double.class || javaClass == Float.class)
            return StandardTypes.getDoubleType();
        if (javaClass == Boolean.class)
            return StandardTypes.getBooleanType();
        if (javaClass == String.class)
            return StandardTypes.getStringType();
        if (javaClass == Date.class)
            return StandardTypes.getTimeType();
        if (javaClass == Password.class)
            return StandardTypes.getPasswordType();
        if (javaClass == Null.class)
            return StandardTypes.getNullType();
        if (javaClass == Object.class)
            return StandardTypes.getAnyType();
        if (javaClass == Table.class)
            return StandardTypes.getAnyArrayType();
        if (javaClass == ReadonlyList.class)
            return StandardTypes.getReadOnlyAnyArrayType();
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

    public static List<ClassInstance> merge(List<ClassInstance> result1, List<ClassInstance> result2, boolean desc, long limit) {
        return sortAndLimit(new ArrayList<>(NncUtils.mergeUnique(result1, result2)), desc, limit);
    }

    public static @Nullable ObjectMappingRefDTO getSourceMappingRefDTO(Instance instance) {
        if (instance instanceof DurableInstance durableInstance)
            return durableInstance.isView() ? durableInstance.getSourceRef().getMappingRefDTO() : null;
        else
            return null;
    }

    public static void reloadParent(Entity entity, DurableInstance instance, ObjectInstanceMap instanceMap, DefContext defContext) {
//        try(var ignored = ContextUtil.getProfiler().enter("ModelDef.reloadParent")) {
        if (entity.getParentEntity() != null) {
            var parent = (DurableInstance) instanceMap.getInstance(entity.getParentEntity());
            Field parentField = null;
            if (entity.getParentEntityField() != null)
                parentField = defContext.getField(entity.getParentEntityField());
            instance.setParentInternal(parent, parentField);
        } else {
            instance.setParentInternal(null, null);
        }
//        }
    }
}
