package org.metavm.util;

import org.metavm.api.ReadonlyList;
import org.metavm.ddl.Commit;
import org.metavm.ddl.FieldChange;
import org.metavm.entity.*;
import org.metavm.entity.natives.CallContext;
import org.metavm.entity.natives.ListNative;
import org.metavm.flow.Flows;
import org.metavm.flow.Method;
import org.metavm.object.instance.ObjectInstanceMap;
import org.metavm.object.instance.core.*;
import org.metavm.object.type.EnumConstantDef;
import org.metavm.object.type.*;
import org.metavm.object.view.rest.dto.ObjectMappingRefDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

public class Instances {

    private static final Logger logger = LoggerFactory.getLogger(Instances.class);

    public static final Map<Class<?>, Type> JAVA_CLASS_TO_BASIC_TYPE = Map.of(
            Integer.class, PrimitiveType.longType,
            Long.class, PrimitiveType.longType,
            Double.class, PrimitiveType.doubleType,
            Boolean.class, PrimitiveType.booleanType,
            String.class, PrimitiveType.stringType,
            Date.class, PrimitiveType.timeType,
            Password.class, PrimitiveType.passwordType,
            Null.class, PrimitiveType.nullType,
            Object.class, AnyType.instance
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

    public static <T extends InstanceReference> List<T> sort(List<T> instances, boolean desc) {
        if (desc)
            instances.sort((i1, i2) -> NncUtils.compareId(i2.tryGetTreeId(), i1.tryGetTreeId()));
        else
            instances.sort((i1, i2) -> NncUtils.compareId(i1.tryGetTreeId(), i2.tryGetTreeId()));
        return instances;
    }

    public static <T extends InstanceReference> List<T> sortAndLimit(List<T> instances, boolean desc, long limit) {
        sort(instances, desc);
        if (limit == -1L)
            return instances;
        else
            return instances.subList(0, Math.min(instances.size(), (int) limit));
    }

    public static int compare(DurableInstance instance1, DurableInstance instance2) {
        if (instance1.isIdInitialized() && instance2.isIdInitialized())
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
                () -> String.format("Can not resolve primitive value '%s", value));
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
        if (instance instanceof InstanceReference r && r.resolve() instanceof ClassInstance clsInst && clsInst.getType().isList()) {
            var listNative = new ListNative(clsInst);
            var array = listNative.toArray();
            return clsInst.getType().getName() + " [" + NncUtils.join(array, Instances::getInstanceDesc) + "]";
        } else
            return getInstanceDesc(instance);
    }

    public static String getInstanceDesc(DurableInstance instance) {
        if (instance.getMappedEntity() != null)
            return EntityUtils.getEntityDesc(instance.getMappedEntity());
        else
            return instance.toString();
    }

    public static String getInstanceDesc(Instance instance) {
        if (instance instanceof InstanceReference r) {
            if (r.resolve().getMappedEntity() != null)
                return EntityUtils.getEntityDesc(r.resolve().getMappedEntity());
            else
                return r.resolve().toString();
        } else
            return instance.toString();
    }

    public static String getInstancePath(DurableInstance instance) {
        if (instance.getMappedEntity() != null)
            return EntityUtils.getEntityPath(instance.getMappedEntity());
        else {
            var path = new LinkedList<DurableInstance>();
            var i = instance;
            while (i != null) {
                path.addFirst(i);
                i = i.getParent();
            }
            return NncUtils.join(path, Instances::getInstanceDesc, "->");
        }
    }

    public static String getInstancePath(Instance instance) {
        if (instance instanceof InstanceReference r) {
            if (r.resolve().getMappedEntity() != null)
                return EntityUtils.getEntityPath(r.resolve().getMappedEntity());
            else {
                var path = new LinkedList<InstanceReference>();
                var i = r.resolve();
                while (i != null) {
                    path.addFirst(i.getReference());
                    i = i.getParent();
                }
                return NncUtils.join(path, Instances::getInstanceDesc, "->");
            }
        } else
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
        return new LongInstance(value, Types.getLongType());
    }

    public static LongInstance longInstance(long value, Function<Class<?>, Type> getTypeFunc) {
        return new LongInstance(value, Types.getLongType());
    }

    public static BooleanInstance booleanInstance(boolean value) {
        return new BooleanInstance(value, Types.getBooleanType());
    }

    public static BooleanInstance booleanInstance(boolean value, Function<Class<?>, Type> getTypeFunc) {
        return new BooleanInstance(value, Types.getBooleanType());
    }

    public static DoubleInstance doubleInstance(double value) {
        return new DoubleInstance(value, Types.getDoubleType());
    }

    public static DoubleInstance doubleInstance(double value, Function<Class<?>, Type> getTypeFunc) {
        return new DoubleInstance(value, Types.getDoubleType());
    }

    public static TimeInstance timeInstance(long value, Function<Class<?>, Type> getTypeFunc) {
        return new TimeInstance(value, Types.getTimeType());
    }

    public static TimeInstance timeInstance(long value) {
        return new TimeInstance(value, Types.getTimeType());
    }

    public static NullInstance nullInstance() {
        return new NullInstance(Types.getNullType());
    }

    public static BooleanInstance trueInstance() {
        return new BooleanInstance(true, Types.getBooleanType());
    }

    public static BooleanInstance falseInstance() {
        return new BooleanInstance(false, Types.getBooleanType());
    }

    public static PasswordInstance passwordInstance(String password) {
        return new PasswordInstance(password, Types.getPasswordType());
    }

    public static PasswordInstance passwordInstance(String password, Function<Class<?>, Type> getTypeFunc) {
        return new PasswordInstance(password, Types.getPasswordType());
    }

    public static StringInstance stringInstance(String value) {
        return new StringInstance(value, Types.getStringType());
    }

    public static StringInstance stringInstance(String value, Function<Class<?>, Type> getTypeFunc) {
        return new StringInstance(value, Types.getStringType());
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
        return new StringInstance(value, Types.getStringType());
    }

    public static LongInstance createLong(long value) {
        return new LongInstance(value, Types.getLongType());
    }

    public static DoubleInstance createDouble(double value) {
        return new DoubleInstance(value, Types.getDoubleType());
    }

    public static BooleanInstance createBoolean(boolean b) {
        return b ? trueInstance() : falseInstance();
    }

    private static ArrayType getAnyArrayType() {
        return new ArrayType(AnyType.instance, ArrayKind.READ_WRITE);
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
            return Types.getLongType();
        if (javaClass == Double.class || javaClass == Float.class)
            return Types.getDoubleType();
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

    public static List<InstanceReference> merge(List<InstanceReference> result1, List<InstanceReference> result2, boolean desc, long limit) {
        return sortAndLimit(new ArrayList<>(NncUtils.mergeUnique(result1, result2)), desc, limit);
    }

    public static @Nullable ObjectMappingRefDTO getSourceMappingRefDTO(Instance instance) {
        if (instance instanceof InstanceReference ref) {
            var durableInstance = ref.resolve();
            return durableInstance.isView() ? durableInstance.getSourceRef().getMappingRefDTO() : null;
        } else
            return null;
    }

    public static void reloadParent(Entity entity, DurableInstance instance, ObjectInstanceMap instanceMap, DefContext defContext) {
//        try(var ignored = ContextUtil.getProfiler().enter("ModelDef.reloadParent")) {
        if (entity.getParentEntity() != null) {
            var parent = (InstanceReference) instanceMap.getInstance(entity.getParentEntity());
            Field parentField = null;
            if (entity.getParentEntityField() != null)
                parentField = defContext.getField(entity.getParentEntityField());
            instance.setParentInternal(parent.resolve(), parentField, true);
        } else {
            instance.setParentInternal(null, null, true);
        }
//        }
    }

    public static ClassInstance createList(ClassType listType, List<? extends Instance> elements) {
        if (listType.isList()) {
            var elementType = listType.getFirstTypeArgument();
            if (listType.getKlass() == StdKlass.list.get()) {
                listType = StdKlass.arrayList.get().getParameterized(List.of(elementType)).getType();
            }
            var list = ClassInstance.allocate(listType);
            var listNative = new ListNative(list);
            listNative.List();
            for (Instance element : elements) {
                listNative.add(element);
            }
            return list;
        } else
            throw new IllegalArgumentException(listType + " is not a List type");
    }

    public static void applyDDL(Iterable<DurableInstance> instances, Commit commit, IEntityContext context) {
        var newFields = NncUtils.map(commit.getNewFieldIds(), context::getField);
        var convertingFields = NncUtils.map(commit.getConvertingFieldIds(), context::getField);
        var toChildFields = NncUtils.map(commit.getToChildFieldIds(), context::getField);
        var changingSuperKlasses = NncUtils.map(commit.getChangingSuperKlassIds(), context::getKlass);
        var toValueKlasses = NncUtils.map(commit.getEntityToValueKlassIds(), context::getKlass);
        var valueToEntityKlasses = NncUtils.map(commit.getValueToEntityKlassIds(), context::getKlass);
        var toEnumKlasses = NncUtils.map(commit.getToEnumKlassIds(), context::getKlass);
        for (DurableInstance instance : instances) {
            if (instance instanceof ClassInstance clsInst) {
                for (Field field : newFields) {
                    var k = clsInst.getKlass().findAncestorKlassByTemplate(field.getDeclaringType());
                    if (k != null) {
                        var pf = k.findField(f -> f.getEffectiveTemplate() == field);
                        initializeField(clsInst, pf, context);
                    }
                }
                for (Field field : convertingFields) {
                    var k = clsInst.getKlass().findAncestorKlassByTemplate(field.getDeclaringType());
                    if (k != null) {
                        var pf = k.findField(f -> f.getEffectiveTemplate() == field);
                        convertField(clsInst, pf, context);
                    }
                }
                for (Field field : toChildFields) {
                    var k = clsInst.getKlass().findAncestorKlassByTemplate(field.getDeclaringType());
                    if (k != null) {
                        var pf = k.findField(f -> f.getEffectiveTemplate() == field);
                        var value = clsInst.getField(pf);
                        if (value instanceof InstanceReference r)
                            r.resolve().setParent(clsInst, pf);
                    }
                }
                for (Klass klass : changingSuperKlasses) {
                    var k = clsInst.getKlass().findAncestorKlassByTemplate(klass);
                    if (k != null)
                        initializeSuper(clsInst, k, context);
                }
                for (Klass klass : toValueKlasses) {
                    handleEntityToValueConversion(clsInst, klass);
                }
            }
            for (Klass klass : valueToEntityKlasses) {
                instance.forEachReference(r -> {
                    if (r.isResolved()) {
                        var resolved = r.resolve();
                        if (resolved instanceof ClassInstance clsInst) {
                            var k = clsInst.getKlass().findAncestorByTemplate(klass);
                            if (k != null)
                                r.setEager();
                        }
                    }
                });
            }
            for (Klass klass : toEnumKlasses) {
                handleEnumConversion(instance, klass, context);
            }
        }
    }

    private static void handleEntityToValueConversion(ClassInstance instance, Klass klass) {
        instance.forEachReference((r, isChild, type) -> {
            if(type.isAssignableFrom(klass.getType())) {
                var referent = r.resolve();
                if(referent instanceof ClassInstance object && object.getKlass().findAncestorKlassByTemplate(klass) != null)
                    r.setEager();
            }
        });
    }

    private static void handleEnumConversion(DurableInstance instance, Klass enumClass, IEntityContext context) {
        instance.forEachReference((r, isChild, type) -> {
            if(type.isAssignableFrom(enumClass.getType())) {
                var referent = r.resolve();
                if(referent instanceof ClassInstance object && object.getKlass() == enumClass && !enumClass.isEnumConstant(object.getReference())) {
                    var r1 = object.getReference();
                    object.setField(enumClass.getFieldByTemplate(StdField.enumName.get()), Instances.stringInstance(""));
                    object.setField(enumClass.getFieldByTemplate(StdField.enumOrdinal.get()), Instances.longInstance(-1L));
                    var forwarded = mapEnumConstant(r1 ,enumClass, context);
                    object.setUnknown(StdKlass.enum_.get().getTag(), Constants.ENUM_CONSTANT_FP_TAG, forwarded);
                    r.setForwarded();
                }
            }
        });
    }

    private static InstanceReference mapEnumConstant(InstanceReference instance, Klass enumClass, IEntityContext context) {
        var mapper = getEnumConstantMapper(enumClass);
        return (InstanceReference) Objects.requireNonNull(Flows.invoke(mapper, null, List.of(instance), context.getInstanceContext()));
    }

    private static Method getEnumConstantMapper(Klass enumClass) {
        var found = enumClass.findMethod(m -> m.isStatic() && m.getName().equals("__map__") && m.getParameterTypes().equals(List.of(enumClass.getType())));
        if(found == null)
            throw new IllegalStateException("Failed to find an enum constant mapper in class " + enumClass.getName());
        return found;
    }

    public static void setEagerFlag(List<DurableInstance> referring, Id id) {
        for (DurableInstance ref : referring) {
            ref.forEachReference(r -> {
                if (r.idEquals(id))
                    r.setEager();
            });
        }
    }

    private static void initializeField(ClassInstance instance, Field field, IEntityContext context) {
        var initialValue = computeFieldInitialValue(instance, field, context.getInstanceContext());
        instance.setField(field, initialValue);
    }

    public static @Nullable Method findFieldInitializer(Field field) {
        var klass = field.getDeclaringType();
        var initMethodName = "__" + field.getCodeNotNull() + "__";
        return klass.findMethodByCodeAndParamTypes(initMethodName, List.of());
    }

    public static @Nullable Instance getDefaultValue(Field field) {
        var type = field.getType();
        if (type.isNullable())
            return Instances.nullInstance();
        else if (type instanceof PrimitiveType primitiveType)
            return primitiveType.getDefaultValue();
        else
            return null;
    }

    public static Instance computeFieldInitialValue(ClassInstance instance, Field field, CallContext callContext) {
        var initMethod = findFieldInitializer(field);
        if (initMethod != null)
            return Flows.invoke(initMethod, instance, List.of(), callContext);
        else
            return Objects.requireNonNull(getDefaultValue(field), "Can not find a default value for field " + field.getQualifiedName());
    }

    private static void convertField(ClassInstance instance, Field field, IEntityContext context) {
        var convertedValue = computeConvertedFieldValue(instance, field, context.getInstanceContext());
        instance.setField(field, convertedValue);
    }

    public static @Nullable Method findTypeConverter(Field field) {
        var klass = field.getDeclaringType();
        var initMethodName = "__" + field.getCodeNotNull() + "__";
        return klass.findMethod(
                m -> initMethodName.equals(m.getCode())
                        && m.getParameters().size() == 1
                        && m.getReturnType().equals(field.getType())
        );
    }

    public static Instance computeConvertedFieldValue(ClassInstance instance, Field field, IInstanceContext context) {
        var converter = Objects.requireNonNull(findTypeConverter(field));
        var originalValue = instance.getUnknownField(field.getDeclaringType().getTag(), field.getOriginalTag());
        return Flows.invoke(converter, instance, List.of(originalValue), context);
    }

    private static void initializeSuper(ClassInstance instance, Klass klass, IEntityContext context) {
        var s = computeSuper(instance, klass, context.getInstanceContext());
        s.forEachField(instance::setField);
    }


    public static Method findSuperInitializer(Klass klass) {
        var superKlass = Objects.requireNonNull(klass.getSuperType()).resolve();
        var initMethodName = "__" + superKlass.getName() + "__";
        return klass.findMethod(
                m -> initMethodName.equals(m.getCode())
                        && m.getParameters().isEmpty()
                        && m.getReturnType().equals(superKlass.getType())
        );
    }

    private static ClassInstance computeSuper(ClassInstance instance, Klass klass, CallContext callContext) {
        var initializer = Objects.requireNonNull(findSuperInitializer(klass));
        var s = Objects.requireNonNull(Flows.invoke(initializer, instance, List.of(), callContext)).resolveObject();
        s.setEphemeral();
        return s;
    }

    public static void rollbackDDL(Iterable<DurableInstance> instances, Commit commit, IEntityContext context) {
        for (FieldChange fieldChange : commit.getFieldChanges()) {
            var klass = context.getKlass(fieldChange.klassId());
            for (DurableInstance instance : instances) {
                if(instance instanceof ClassInstance object) {
                    var k = object.getKlass().findAncestorByTemplate(klass);
                    if(k != null)
                        object.tryClearUnknownField(k.getTag(), fieldChange.newTag());
                }
            }
        }
        for (String toChildFieldId : commit.getToChildFieldIds()) {
            var field = context.getField(toChildFieldId);
            for (DurableInstance instance : instances) {
                if (instance instanceof ClassInstance object) {
                  var k = object.getKlass().findAncestorByTemplate(field.getDeclaringType());
                  if(k != null) {
                      var value = object.getField(field);
                      if(value instanceof InstanceReference ref)
                          ref.resolve().clearParent();
                  }
                }
            }
        }
        for (List<String> klassIdsList : List.of(commit.getEntityToValueKlassIds(), commit.getValueToEntityKlassIds())) {
            for (String klassId : klassIdsList) {
                var klass = context.getKlass(klassId);
                for (DurableInstance instance : instances) {
                    instance.forEachReference(r -> {
                        if(r.isEager()) {
                            var referent = r.resolve();
                            if(referent instanceof ClassInstance object && object.getKlass().findAncestorKlassByTemplate(klass) != null)
                                r.clearEager();
                        }
                    });
                }
            }
        }
        for (String klassId : commit.getToEnumKlassIds()) {
            var klass = context.getKlass(klassId);
            for (DurableInstance instance : instances) {
                instance.forEachReference((r, isChild, type) -> {
                    if(r.isForwarded() && type.isAssignableFrom(klass.getType())) {
                        var resolved = r.resolve();
                        if(resolved instanceof ClassInstance object && object.getKlass() == klass) {
                            r.clearForwarded();
                            object.tryClearUnknownField(StdKlass.enum_.get().getTag(), Constants.ENUM_CONSTANT_FP_TAG);
                            object.tryClearUnknownField(StdKlass.enum_.get().getTag(), StdField.enumName.get().getTag());
                            object.tryClearUnknownField(StdKlass.enum_.get().getTag(), StdField.enumOrdinal.get().getTag());
                        }
                    }
                });
            }
        }
    }

    public static void saveEnumConstants(Commit commit, IEntityContext context) {
        for (String newEnumConstantId : commit.getNewEnumConstantIds()) {
            var ecd = context.getEntity(EnumConstantDef.class, newEnumConstantId);
            var klass = ecd.getKlass();
            var field = klass.getStaticFieldByName(ecd.getName());
            var value = ecd.createEnumConstant(context.getInstanceContext());
            field.setStaticValue(value.getReference());
        }
    }

}
