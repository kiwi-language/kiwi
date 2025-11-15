package org.metavm.util;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.metavm.api.ReadonlyList;
import org.metavm.beans.BeanDefinitionRegistry;
import org.metavm.ddl.Commit;
import org.metavm.entity.Entity;
import org.metavm.entity.EntityUtils;
import org.metavm.entity.StdField;
import org.metavm.entity.natives.CallContext;
import org.metavm.flow.Flows;
import org.metavm.flow.Method;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.instance.core.*;
import org.metavm.object.type.*;

import javax.annotation.Nullable;
import java.util.*;
import java.util.LinkedList;

import static java.util.Objects.requireNonNull;

@Slf4j
public class Instances {

    public static final Map<Class<?>, Type> JAVA_CLASS_TO_BASIC_TYPE = Map.ofEntries(
            Map.entry(Integer.class, PrimitiveType.intType),
            Map.entry(Short.class, PrimitiveType.shortType),
            Map.entry(Byte.class, PrimitiveType.byteType),
            Map.entry(Long.class, PrimitiveType.longType),
            Map.entry(Character.class, PrimitiveType.charType),
            Map.entry(Double.class, PrimitiveType.doubleType),
            Map.entry(Float.class, PrimitiveType.floatType),
            Map.entry(Boolean.class, PrimitiveType.booleanType),
            Map.entry(Date.class, PrimitiveType.timeType),
            Map.entry(Password.class, PrimitiveType.passwordType),
            Map.entry(Null.class, NullType.instance),
            Map.entry(Object.class, AnyType.instance)
    );

    public static final Map<Type, Class<?>> BASIC_TYPE_JAVA_CLASS;

    public static final Map<Class<?>, Class<?>> JAVA_CLASS_TO_INSTANCE_CLASS = Map.ofEntries(
            Map.entry(String.class, StringReference.class),
            Map.entry(Integer.class, IntValue.class),
            Map.entry(Long.class, LongValue.class),
            Map.entry(Double.class, DoubleValue.class),
            Map.entry(Float.class, FloatValue.class),
            Map.entry(Boolean.class, BooleanValue.class),
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
            instances.sort((i1, i2) -> Utils.compareId(((EntityReference) i2).tryGetTreeId(), ((EntityReference) i1).tryGetTreeId()));
        else
            instances.sort((i1, i2) -> Utils.compareId(((EntityReference) i1).tryGetTreeId(), ((EntityReference) i2).tryGetTreeId()));
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
        return instance1.getId().compareTo(instance2.getId());
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

    public static ClassInstance classInstance(Klass type, Map<Field, Value> fields, boolean isNew) {
        return new MvClassInstance(null, fields, type, isNew);
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

    public static Value stringInstance(String value) {
        return new StringInstance(value).getReference();
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

    public static BooleanValue createBoolean(boolean b) {
        return b ? trueInstance() : falseInstance();
    }

    private static ArrayType getAnyArrayType() {
        return new ArrayType(AnyType.instance, ArrayKind.DEFAULT);
    }

    public static ArrayInstance createArray(List<? extends Value> instances) {
        return createArray(getAnyArrayType(), instances);
    }


    public static ArrayInstance createArray(ArrayType type, List<? extends Value> instances) {
        return new ArrayInstance(type, instances);
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

    public static void migrate(Iterable<? extends Instance> instances, Commit commit, IInstanceContext context) {
        var tracing = DebugEnv.traceMigration;
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
            if (tracing)
                log.trace("Migrating instance {}, tree ID: {}", instance, instance.tryGetTreeId());
            if (instance instanceof MvInstance mvInst/* && !mvInst.isEnum()*/) {
                migrate(mvInst, newFields, convertingFields, changingSuperKlasses, toValueKlasses,
                        valueToEntityKlasses, toEnumKlasses, removingChildFields, runMethods, newIndexes, searchEnabledKlasses,
                        commit, context);
            }
        }
    }

    public static void migrate(MvInstance instance,
                               Collection<Field> newFields,
                               Collection<Field> convertingFields,
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
            for (Klass klass : changingSuperKlasses) {
                var k = clsInst.getInstanceType().asSuper(klass);
                if (k != null)
                    initializeSuper(clsInst, klass, context);
            }
            for (Klass klass : toValueKlasses) {
                handleEntityToValueConversion((MvClassInstance) clsInst, klass);
            }
            for (Klass klass : valueToEntityKlasses) {
                handleValueToEntityConversion((MvClassInstance) clsInst, klass, context);
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
//                if (!ref.referenced) {
//                    var referring = context.getByReferenceTargetId(child.getId(), 0, 1);
//                    if (!referring.isEmpty())
//                        ref.referenced = true;
//                }
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
        for (Klass klass : toEnumKlasses) {
            handleEnumConversion(instance, klass, redirectStatus, context);
        }
    }

    public static void remove(Iterable<Instance> instances, IInstanceContext context) {
        for (Instance instance : instances) {
            if (instance instanceof ClassInstance clsInst) {
                if (clsInst.getInstanceKlass().getState() == ClassTypeState.REMOVING) {
                    context.remove(clsInst);
                }
            }
        }
    }

    private static void handleValueToEntityConversion(MvClassInstance instance, Klass klass, IInstanceContext context) {
        instance.transformReference((r, isChild, type) -> {
            if (type.isAssignableFrom(klass.getType())) {
                if (r.get() instanceof ClassInstance object && object.getInstanceType().asSuper(klass) != null) {
                    object.setRemoved();
                    return object.copy(context::allocateRootId).getReference();
                }
            }
            return r;
        });
    }

    private static void handleEntityToValueConversion(MvClassInstance instance, Klass klass) {
        instance.transformReference((r, isChild, type) -> {
            if (type.isAssignableFrom(klass.getType())) {
                if (r.get() instanceof ClassInstance object && object.getInstanceType().asSuper(klass) != null) {
                    if (!object.isRemoved())
                        object.setRemoved();
                    var r1 =  object.copy(getCopyType(object.getInstanceType()), t -> null).getReference();
                    Utils.require(r1 instanceof ValueReference);
                    return r1;
                }
            }
            return r;
        });
    }

    private static ClassType getCopyType(@NotNull ClassType type) {
        var t = type;
        while (t != null && t.getKlass().getState() == ClassTypeState.REMOVING)
            t = t.getSuperType();
        if (t == null || t.isAbstract())
            throw new IllegalStateException(
                    "Can not copy instance of type " + type.getTypeDesc() + " because it is abstract or removed");
        return t;
    }

    private static void handleEnumConversion(MvInstance instance, Klass enumClass, @Nullable RedirectStatus redirectStatus, IInstanceContext context) {
        instance.transformReference((r, isChild, type) -> {
            if (type.isAssignableFrom(enumClass.getType())) {
                var referent = r.get();
                var sft = StaticFieldTable.getInstance(enumClass.getType(), context);
                if (referent instanceof ClassInstance object && object.getInstanceKlass() == enumClass && !sft.isEnumConstant(object.getReference())) {
                    var r1 = object.getReference();
                    object.setFieldForce(StdField.enumName.get(), Instances.stringInstance(""));
                    object.setFieldForce(StdField.enumOrdinal.get(), Instances.intInstance(-1));
                    return mapEnumConstant(r1, enumClass, context);
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
//            else if (initMethod.getParameterTypes().equals(List.of(Types.getStringType(), Types.getIntType()))) {
//                return field.getType().fromStackValue(Flows.invoke(
//                        initMethod.getRef(),
//                        instance,
//                        List.of(
////                                instance.getUnknownField(StdKlass.enum_.get().getTag(), StdField.enumName.get().getTag()),
////                                instance.getUnknownField(StdKlass.enum_.get().getTag(), StdField.enumOrdinal.get().getTag())
//                        ),
//                        context
//                ));
//            }
            else
                throw new IllegalStateException("Invalid initializer method: " + initMethod.getSignatureString());
        } else if (field.getInitializer() != null)
            return field.getType().fromStackValue(Flows.invoke(field.getInitializer().getRef(), instance, List.of(), context));
        else
            return requireNonNull(getDefaultValue(field, context), "Can not find a default value for field " + field.getQualifiedName());
    }

    public static void convertField(ClassInstance instance, Field field, IInstanceContext context) {
        if (DebugEnv.traceDDL) {
            log.trace("Converting field {} for instance {}, tag: {}, type: {}",
                    field.getQualifiedName(), instance, field.getTag(), field.getType().getTypeDesc());
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
        var originalValue = instance.getField(field);
        if (field.getType().isInstance(originalValue))
            return originalValue;
        else
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
        var tracing = DebugEnv.traceMigration;
        var superInitializer = findSuperInitializer(klass);
        if (tracing)
            log.trace("Initializing super klass for instance {} with initializer {}", instance, superInitializer);
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

    public static int hashCode(Value value, CallContext callContext) {
        if (value instanceof PrimitiveValue primitiveValue)
            return primitiveValue.hashCode();
        else if (value instanceof StringReference s)
            return s.hashCode();
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
            case PrimitiveValue primitiveValue -> Objects.toString(primitiveValue.getValue());
            case NullValue ignored -> "null";
            case StringReference s -> s.getValue();
            case Reference reference -> toString(reference.get(), callContext);
            case null, default -> throw new IllegalArgumentException("Cannot convert value '" + value + "' to string");
        };
    }

    public static String toString(Instance instance, CallContext callContext) {
        if (instance instanceof ClassInstance clsInst) {
            var method = clsInst.getInstanceKlass().getToStringMethod();
            if (method != null) {
                var ret = Flows.invoke(method.getRef(), clsInst, List.of(), callContext);
                return toJavaString(requireNonNull(ret));
            }
            var titleField = clsInst.getInstanceKlass().getTitleField();
            if (titleField != null && clsInst.isFieldInitialized(titleField)
                    && clsInst.getField(titleField) instanceof StringReference s)
                return s.getValue();
            else if (!instance.isValue())
                return instance.getStringId();
        }
        return instance.getInstanceType().getTypeDesc(); // + "@" + instance.getStringId();
    }

    public static int toInt(@Nullable Value value) {
        return ((IntValue) requireNonNull(value)).value;
    }

    public static boolean toBoolean(@Nullable Value value) {
        return ((IntValue) requireNonNull(value)).value != 0;
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

    public static IntValue intInstance(int i) {
        if (i == -1) return IntValue.minusOne;
        if (i == 0) return IntValue.zero;
        if (i == 1) return IntValue.one;
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

    public static String toJavaString(Value value) {
        if (value instanceof NullValue)
            return null;
        return ((StringReference) value).getValue();
    }

    /**
     * Clear marks and return unmarked instances
     * @return unmarked
     */
    public static <T extends Instance> List<T> clearMarks(List<T> instances) {
        var marked = new ArrayList<T>();
        for (T inst : instances) {
            if (inst.isMarked()) {
                inst.clearMarked();
                marked.add(inst);
            }
        }
        return marked;
    }

}
