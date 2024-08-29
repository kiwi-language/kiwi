package org.metavm.util;

import org.metavm.api.ReadonlyList;
import org.metavm.beans.BeanDefinitionRegistry;
import org.metavm.ddl.Commit;
import org.metavm.ddl.FieldChange;
import org.metavm.entity.*;
import org.metavm.entity.natives.CallContext;
import org.metavm.entity.natives.ListNative;
import org.metavm.flow.Flows;
import org.metavm.flow.Method;
import org.metavm.object.instance.ObjectInstanceMap;
import org.metavm.object.instance.core.Reference;
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
            Map.entry(Integer.class, LongValue.class),
            Map.entry(Long.class, LongValue.class),
            Map.entry(Double.class, DoubleValue.class),
            Map.entry(Boolean.class, BooleanValue.class),
            Map.entry(String.class, StringValue.class),
            Map.entry(Date.class, TimeValue.class),
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

    public static BooleanValue equals(Value first, Value second) {
        return createBoolean(Objects.equals(first, second));
    }

    public static BooleanValue notEquals(Value first, Value second) {
        return createBoolean(!Objects.equals(first, second));
    }

    public static boolean isAllTime(Value instance1, Value instance2) {
        return instance1 instanceof TimeValue || instance2 instanceof TimeValue;
    }

    public static boolean isAllIntegers(Value instance1, Value instance2) {
        return isInteger(instance1) && isInteger(instance2);
    }

    public static boolean isAllNumbers(Value instance1, Value instance2) {
        return isNumber(instance1) && isNumber(instance2);
    }

    public static boolean isNumber(Value instance) {
        return isInteger(instance) || instance instanceof DoubleValue;
    }

    public static <T extends Reference> List<T> sort(List<T> instances, boolean desc) {
        if (desc)
            instances.sort((i1, i2) -> NncUtils.compareId(i2.tryGetTreeId(), i1.tryGetTreeId()));
        else
            instances.sort((i1, i2) -> NncUtils.compareId(i1.tryGetTreeId(), i2.tryGetTreeId()));
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

    private static boolean isInteger(Value instance) {
        return instance instanceof LongValue;
    }

    public static boolean isAnyNull(Value... instances) {
        for (Value instance : instances) {
            if (instance instanceof NullValue) {
                return true;
            }
        }
        return false;
    }

    public static ArrayInstance arrayInstance(ArrayType type, List<Value> elements) {
        return new ArrayInstance(type, elements);
    }

    public static ClassInstance classInstance(Klass type, Map<Field, Value> fields) {
        return new ClassInstance(null, fields, type);
    }

    public static PrimitiveValue serializePrimitive(Object value, Function<Class<?>, Type> getTypeFunc) {
        return NncUtils.requireNonNull(trySerializePrimitive(value, getTypeFunc),
                () -> String.format("Can not resolve primitive value '%s", value));
    }

    public static boolean isPrimitive(Object value) {
        return value == null || value instanceof Date || value instanceof String ||
                value instanceof Boolean || value instanceof Password ||
                ValueUtils.isInteger(value) || ValueUtils.isFloat(value);
    }

    public static @Nullable PrimitiveValue trySerializePrimitive(Object value, Function<Class<?>, Type> getTypeFunc) {
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

    public static String getInstanceDetailedDesc(Value instance) {
        if (instance instanceof Reference r && r.resolve() instanceof ClassInstance clsInst && clsInst.getType().isList()) {
            var listNative = new ListNative(clsInst);
            var array = listNative.toArray();
            return clsInst.getType().getName() + " [" + NncUtils.join(array, Instances::getInstanceDesc) + "]";
        } else
            return getInstanceDesc(instance);
    }

    public static String getInstanceDesc(Instance instance) {
        if (instance.getMappedEntity() != null)
            return EntityUtils.getEntityDesc(instance.getMappedEntity());
        else
            return instance.toString();
    }

    public static String getInstanceDesc(Value instance) {
        if (instance instanceof Reference r) {
            if (r.resolve().getMappedEntity() != null)
                return EntityUtils.getEntityDesc(r.resolve().getMappedEntity());
            else
                return r.resolve().toString();
        } else
            return instance.toString();
    }

    public static String getInstancePath(Instance instance) {
        if (instance.getMappedEntity() != null)
            return EntityUtils.getEntityPath(instance.getMappedEntity());
        else {
            var path = new LinkedList<Instance>();
            var i = instance;
            while (i != null) {
                path.addFirst(i);
                i = i.getParent();
            }
            return NncUtils.join(path, Instances::getInstanceDesc, "->");
        }
    }

    public static String getInstancePath(Value instance) {
        if (instance instanceof Reference r) {
            if (r.resolve().getMappedEntity() != null)
                return EntityUtils.getEntityPath(r.resolve().getMappedEntity());
            else {
                var path = new LinkedList<Reference>();
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
    public static <T> T deserializePrimitive(PrimitiveValue instance, Class<T> javaClass) {
        javaClass = (Class<T>) ReflectionUtils.getBoxedClass(javaClass);
        if (instance.isNull()) {
            return null;
        }
        if (instance instanceof LongValue longInstance) {
            if (javaClass == int.class || javaClass == Integer.class)
                return (T) Integer.valueOf(longInstance.getValue().intValue());
            else if (javaClass == short.class || javaClass == Short.class)
                return (T) Short.valueOf(longInstance.getValue().shortValue());
            else if (javaClass == byte.class || javaClass == Byte.class)
                return (T) Byte.valueOf(longInstance.getValue().byteValue());
            else
                return javaClass.cast(longInstance.getValue());
        }
        if (instance instanceof DoubleValue doubleInstance) {
            if (javaClass == float.class || javaClass == Float.class)
                return (T) Float.valueOf(doubleInstance.getValue().floatValue());
            else if (javaClass == double.class || javaClass == Double.class)
                return javaClass.cast(doubleInstance.getValue());
        }
        if (instance instanceof PasswordValue passwordInstance) {
            return javaClass.cast(new Password(passwordInstance));
        }
        if (instance instanceof TimeValue timeInstance) {
            return javaClass.cast(new Date(timeInstance.getValue()));
        }
        return javaClass.cast(instance.getValue());
    }

    public static PrimitiveValue primitiveInstance(Object value) {
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

    public static LongValue longInstance(long value) {
        return new LongValue(value, Types.getLongType());
    }

    public static LongValue longInstance(long value, Function<Class<?>, Type> getTypeFunc) {
        return new LongValue(value, Types.getLongType());
    }

    public static BooleanValue booleanInstance(boolean value) {
        return new BooleanValue(value, Types.getBooleanType());
    }

    public static BooleanValue booleanInstance(boolean value, Function<Class<?>, Type> getTypeFunc) {
        return new BooleanValue(value, Types.getBooleanType());
    }

    public static DoubleValue doubleInstance(double value) {
        return new DoubleValue(value, Types.getDoubleType());
    }

    public static DoubleValue doubleInstance(double value, Function<Class<?>, Type> getTypeFunc) {
        return new DoubleValue(value, Types.getDoubleType());
    }

    public static TimeValue timeInstance(long value, Function<Class<?>, Type> getTypeFunc) {
        return new TimeValue(value, Types.getTimeType());
    }

    public static TimeValue timeInstance(long value) {
        return new TimeValue(value, Types.getTimeType());
    }

    public static NullValue nullInstance() {
        return new NullValue(Types.getNullType());
    }

    public static BooleanValue trueInstance() {
        return new BooleanValue(true, Types.getBooleanType());
    }

    public static BooleanValue falseInstance() {
        return new BooleanValue(false, Types.getBooleanType());
    }

    public static PasswordValue passwordInstance(String password) {
        return new PasswordValue(password, Types.getPasswordType());
    }

    public static PasswordValue passwordInstance(String password, Function<Class<?>, Type> getTypeFunc) {
        return new PasswordValue(password, Types.getPasswordType());
    }

    public static StringValue stringInstance(String value) {
        return new StringValue(value, Types.getStringType());
    }

    public static StringValue stringInstance(String value, Function<Class<?>, Type> getTypeFunc) {
        return new StringValue(value, Types.getStringType());
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
        var newInstances = NncUtils.filter(
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
        return new StringValue(value, Types.getStringType());
    }

    public static LongValue createLong(long value) {
        return new LongValue(value, Types.getLongType());
    }

    public static DoubleValue createDouble(double value) {
        return new DoubleValue(value, Types.getDoubleType());
    }

    public static BooleanValue createBoolean(boolean b) {
        return b ? trueInstance() : falseInstance();
    }

    private static ArrayType getAnyArrayType() {
        return new ArrayType(AnyType.instance, ArrayKind.READ_WRITE);
    }

    public static ArrayInstance createArray() {
        return createArray(List.of());
    }

    public static ArrayInstance createArray(List<Value> instances) {
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
        return sortAndLimit(new ArrayList<>(NncUtils.mergeUnique(result1, result2)), desc, limit);
    }

    public static @Nullable ObjectMappingRefDTO getSourceMappingRefDTO(Value instance) {
        if (instance instanceof Reference ref) {
            var durableInstance = ref.resolve();
            return durableInstance.isView() ? durableInstance.getSourceRef().getMappingRefDTO() : null;
        } else
            return null;
    }

    public static void reloadParent(Entity entity, Instance instance, ObjectInstanceMap instanceMap, DefContext defContext) {
//        try(var ignored = ContextUtil.getProfiler().enter("ModelDef.reloadParent")) {
        if (entity.getParentEntity() != null) {
            var parent = (Reference) instanceMap.getInstance(entity.getParentEntity());
            Field parentField = null;
            if (entity.getParentEntityField() != null)
                parentField = defContext.getField(entity.getParentEntityField());
            instance.setParentInternal(parent.resolve(), parentField, true);
        } else {
            instance.setParentInternal(null, null, true);
        }
//        }
    }

    public static ClassInstance createList(ClassType listType, List<? extends Value> elements) {
        if (listType.isList()) {
            var elementType = listType.getFirstTypeArgument();
            if (listType.getKlass() == StdKlass.list.get()) {
                listType = StdKlass.arrayList.get().getParameterized(List.of(elementType)).getType();
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

    public static void applyDDL(Iterable<Instance> instances, Commit commit, IEntityContext context) {
        var instCtx = context.getInstanceContext();
        var newFields = NncUtils.map(commit.getNewFieldIds(), context::getField);
        var convertingFields = NncUtils.map(commit.getConvertingFieldIds(), context::getField);
        var toChildFields = NncUtils.map(commit.getToChildFieldIds(), context::getField);
        var changingSuperKlasses = NncUtils.map(commit.getChangingSuperKlassIds(), context::getKlass);
        var toValueKlasses = NncUtils.map(commit.getEntityToValueKlassIds(), context::getKlass);
        var valueToEntityKlasses = NncUtils.map(commit.getValueToEntityKlassIds(), context::getKlass);
        var toEnumKlasses = NncUtils.map(commit.getToEnumKlassIds(), context::getKlass);
        var removingChildFields = NncUtils.map(commit.getRemovedChildFieldIds(), context::getField);
        for (Instance instance : instances) {
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
                        if (value instanceof Reference r)
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
                for (Field removingChildField : removingChildFields) {
                    var k = clsInst.getKlass().findAncestorByTemplate(removingChildField.getDeclaringType());
                    if(k == null)
                        continue;
                    var f = k.getFieldByTemplate(removingChildField);
                    var childRef = clsInst.getField(f);
                    if(childRef.isNull())
                        continue;
                    var child = childRef.resolveDurable();
                    var ref = new Object() {
                        boolean referenced;
                    };
                    var r = child.getReference();
                    child.getRoot().forEachDescendant(i -> {
                        i.forEachReference((ii, isChild) -> {
                            if(!isChild && ii.equals(r))
                                ref.referenced = true;
                        });
                    });
                    if(!ref.referenced) {
                        var referring = instCtx.getByReferenceTargetId(child.getId(), 0, 1);
                        if(!referring.isEmpty())
                            ref.referenced = true;
                    }
                    if(ref.referenced)
                        throw new InternalException(
                                "Unable to delete child field " +
                                        f.getQualifiedName()
                                        + " because the child object " + child + " is still referenced by other objects"
                        );
                    child.setRemoving(true);
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
                handleEnumConversion(instance, klass, commit, context);
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

    private static void handleEnumConversion(Instance instance, Klass enumClass, Commit commit, IEntityContext context) {
        instance.transformReference((r, isChild, type) -> {
            if(type.isAssignableFrom(enumClass.getType())) {
                var referent = r.resolve();
                if(referent instanceof ClassInstance object && object.getKlass() == enumClass && !enumClass.isEnumConstant(object.getReference())) {
                    var r1 = object.getReference();
                    object.setField(enumClass.getFieldByTemplate(StdField.enumName.get()), Instances.stringInstance(""));
                    object.setField(enumClass.getFieldByTemplate(StdField.enumOrdinal.get()), Instances.longInstance(-1L));
                    var ec = mapEnumConstant(r1 ,enumClass, context);
                    return new RedirectingReference(referent, ec, commit);
                }
            }
            return r;
        });
    }

    private static Reference mapEnumConstant(Reference instance, Klass enumClass, IEntityContext context) {
        var mapper = getEnumConstantMapper(enumClass);
        return (Reference) Objects.requireNonNull(Flows.invoke(mapper, null, List.of(instance), context.getInstanceContext()));
    }

    private static Method getEnumConstantMapper(Klass enumClass) {
        var found = enumClass.findMethod(m -> m.isStatic() && m.getName().equals("__map__") && m.getParameterTypes().equals(List.of(enumClass.getType())));
        if(found == null)
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

    public static void initializeField(ClassInstance instance, Field field, IEntityContext context) {
        var initialValue = computeFieldInitialValue(instance, field, context);
        instance.setField(field, initialValue);
    }

    public static @Nullable Method findFieldInitializer(Field field, boolean includeFromEnum) {
        var klass = field.getDeclaringType();
        var initMethodName = "__" + field.getCodeNotNull() + "__";
        var found = klass.findMethodByCodeAndParamTypes(initMethodName, List.of());
        if(found != null || !includeFromEnum)
            return found;
        return klass.findMethodByCodeAndParamTypes(initMethodName, List.of(Types.getStringType(), Types.getLongType()));
    }

    public static @Nullable Value getDefaultValue(Field field, IEntityContext context) {
        var type = field.getType();
        if (type.isNullable())
            return Instances.nullInstance();
        else if (type instanceof PrimitiveType primitiveType)
            return primitiveType.getDefaultValue();
        else if(field.getType() instanceof ClassType ct) {
            var beanDefReg = BeanDefinitionRegistry.getInstance(context);
            var beans = beanDefReg.getBeansOfType(ct);
            if(!beans.isEmpty())
                return beans.get(0).getReference();
        }
        return null;
    }

    public static Value computeFieldInitialValue(ClassInstance instance, Field field, IEntityContext context) {
        var callContext = context.getInstanceContext();
        var initMethod = findFieldInitializer(field, true);
        if (initMethod != null) {
            if(initMethod.getParameters().isEmpty())
                return Flows.invoke(initMethod, instance, List.of(), callContext);
            else if(initMethod.getParameterTypes().equals(List.of(Types.getStringType(), Types.getLongType()))){
                return Flows.invoke(
                        initMethod,
                        instance,
                        List.of(
                                instance.getUnknownField(StdKlass.enum_.get().getTag(), StdField.enumName.get().getTag()),
                                instance.getUnknownField(StdKlass.enum_.get().getTag(), StdField.enumOrdinal.get().getTag())
                        ),
                        callContext
                );
            }
            else
                throw new IllegalStateException("Invalid initializer method: " + initMethod.getSignatureString());
        }
        else
            return Objects.requireNonNull(getDefaultValue(field, context), "Can not find a default value for field " + field.getQualifiedName());
    }

    public static void convertField(ClassInstance instance, Field field, IEntityContext context) {
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

    public static Value computeConvertedFieldValue(ClassInstance instance, Field field, IInstanceContext context) {
        var converter = Objects.requireNonNull(findTypeConverter(field));
        var originalValue = instance.getUnknownField(field.getDeclaringType().getTag(), field.getOriginalTag());
        return Flows.invoke(converter, instance, List.of(originalValue), context);
    }

    private static void initializeSuper(ClassInstance instance, Klass klass, IEntityContext context) {
        computeSuper(instance, klass, context);
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

    private static void computeSuper(ClassInstance instance, Klass klass, IEntityContext context) {
        var superInitializer = findSuperInitializer(klass);
        if(superInitializer != null) {
            var initializer = Objects.requireNonNull(superInitializer);
            var s = Objects.requireNonNull(Flows.invoke(initializer, instance, List.of(), context.getInstanceContext())).resolveObject();
            s.setEphemeral();
            s.forEachField(instance::setFieldForce);
        }
        else {
            var superKlass = Objects.requireNonNull(klass.getSuperType()).resolve();
            superKlass.forEachField(field -> {
                instance.setFieldForce(field,
                        Objects.requireNonNull(Instances.getDefaultValue(field, context),
                                () -> "Default value is missing for field: " + field.getQualifiedName()));
            });
        }
    }

    public static void rollbackDDL(Iterable<Instance> instances, Commit commit, IEntityContext context) {
        for (FieldChange fieldChange : commit.getFieldChanges()) {
            var klass = context.getKlass(fieldChange.klassId());
            for (Instance instance : instances) {
                if(instance instanceof ClassInstance object) {
                    var k = object.getKlass().findAncestorByTemplate(klass);
                    if(k != null)
                        object.tryClearUnknownField(k.getTag(), fieldChange.newTag());
                }
            }
        }
        for (Instance instance : instances) {
            for (String toChildFieldId : commit.getToChildFieldIds()) {
                var field = context.getField(toChildFieldId);
                if (instance instanceof ClassInstance object) {
                  var k = object.getKlass().findAncestorByTemplate(field.getDeclaringType());
                  if(k != null) {
                      var value = object.getField(field);
                      if(value instanceof Reference ref)
                          ref.resolve().clearParent();
                  }
                }
            }
            if(instance.isRemoving())
                instance.setRemoving(false);
        }
        for (List<String> klassIdsList : List.of(commit.getEntityToValueKlassIds(), commit.getValueToEntityKlassIds())) {
            for (String klassId : klassIdsList) {
                var klass = context.getKlass(klassId);
                for (Instance instance : instances) {
                    instance.forEachReference(r -> {
                        if(r.isEager()) {
                            var referent = r.resolve();
                            if(referent instanceof ClassInstance object && object.getKlass().findAncestorKlassByTemplate(klass) != null) {
                                if(object.isValue())
                                    context.getInstanceContext().remove(object);
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
                instance.transformReference((r, isChild, type) -> {
                    if(r instanceof RedirectingReference && type.isAssignableFrom(klass.getType())) {
                        if(r.resolve() instanceof ClassInstance object && object.getKlass() == klass) {
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

    public static void saveEnumConstants(Commit commit, IEntityContext context) {
        for (String newEnumConstantId : commit.getNewEnumConstantIds()) {
            var ecd = context.getEntity(EnumConstantDef.class, newEnumConstantId);
            var klass = ecd.getKlass();
            var field = klass.getStaticFieldByName(ecd.getName());
            var value = ecd.createEnumConstant(context.getInstanceContext());
            field.setStaticValue(value.getReference());
        }
    }

    public static int hashCode(Value value, CallContext callContext) {
        if(value instanceof PrimitiveValue primitiveValue)
            return primitiveValue.hashCode();
        else if(value instanceof Reference reference)
            return hashCode(reference.resolve(), callContext);
        else
            throw new IllegalArgumentException("Cannot get hash code for value: " + value);
    }

    public static int hashCode(Instance instance, CallContext callContext) {
        if(instance instanceof ClassInstance clsInst) {
            var method = clsInst.getKlass().getHashCodeMethod();
            if(method != null) {
                var ret = Flows.invoke(method, clsInst, List.of(), callContext);
                return ((LongValue) Objects.requireNonNull(ret)).getValue().intValue();
            }
            if(clsInst.isValue()) {
                var ref = new Object() {
                    int hash;
                };
                clsInst.forEachField((f, v) -> ref.hash = ref.hash * 31 + hashCode(v, callContext));
                return ref.hash;
            }
        }
        else if(instance instanceof ArrayInstance array && array.isValue()) {
            int h = 0;
            for (Value element : array.getElements())
                h = h * 31 + hashCode(element, callContext);
            return h;
        }
        return instance.hashCode();
    }

    public static boolean equals(Value value1, Value value2, CallContext callContext) {
        if(value1.equals(value2))
            return true;
        else if(value1 instanceof Reference ref1 && value2 instanceof Reference ref2)
            return equals(ref1.resolve(), ref2.resolve(), callContext);
        else
            return false;
    }

    public static boolean equals(Instance instance1, Instance instance2, CallContext callContext) {
        if(instance1 == instance2)
            return true;
        if(instance1 instanceof ClassInstance clsInst) {
            var method = clsInst.getKlass().getEqualsMethod();
            if(method != null) {
                var ret = Flows.invoke(method, clsInst, List.of(instance2.getReference()), callContext);
                return ((BooleanValue) Objects.requireNonNull(ret)).getValue();
            }
            if(clsInst.isValue() && instance2 instanceof ClassInstance clsInst2 && clsInst2.isValue()
                    && clsInst.getType().equals(clsInst2.getType())) {
                var ref = new Object() {
                    boolean equals = true;
                };
                clsInst.forEachField((f, v) -> {
                    if(ref.equals && !equals(v, clsInst2.getField(f), callContext)) {
                        ref.equals = false;
                    }
                });
                return ref.equals;
            }
        }
        else if(instance1 instanceof ArrayInstance array1 && array1.isValue() && instance2 instanceof ArrayInstance array2 && array2.isValue()
                && array1.getType().equals(array2.getType()) && array1.size() == array2.size()) {
            int i = 0;
            for (Value element : array1) {
                if(!equals(element, array2.get(i++), callContext))
                    return false;
            }
            return true;
        }
        return false;
    }
}
