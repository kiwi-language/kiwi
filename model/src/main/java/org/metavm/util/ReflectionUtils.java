package org.metavm.util;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.metavm.entity.Entity;
import sun.misc.Unsafe;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.IntPredicate;
import java.util.function.Predicate;

@Slf4j
public class ReflectionUtils {

    public static final Unsafe UNSAFE;

    public static final String META_VM_PKG = "org.metavm";

    private static final Map<FieldKey, Field> fieldMap = new ConcurrentHashMap<>();

    private record FieldKey(Class<?> klass, String name) {}

    public static final Field ENUM_NAME_FIELD = getField(Enum.class, "name");

    public static final Field ENUM_ORDINAL_FIELD = getField(Enum.class, "ordinal");

    public static final String CONSTRUCTOR_NAME = "<init>";

    public static final MethodHandle entityReadHandle;

    static {
        entityReadHandle = getMethodHandleWithSpread(MethodHandles.lookup(), Entity.class, "read", void.class, List.of(MvInput.class, Entity.class), false);
    }

    public static final Map<Class<?>, String> PRIMITIVE_CLASS_INTERNAL_NAME_MAP = Map.of(
            byte.class, "B",
            short.class, "S",
            int.class, "I",
            long.class, "J",
            float.class, "F",
            double.class, "D",
            boolean.class, "Z",
            char.class, "C",
            void.class, "V"
    );

    public static final Map<String, Class<?>> INTERNAL_NAME_2_PRIMITIVE_CLASS;

    static {
        Field unsafeField = getDeclaredField(Unsafe.class, "theUnsafe");
        unsafeField.setAccessible(true);
        UNSAFE = (Unsafe) get(null, unsafeField);
        Map<String, Class<?>> internalName2PrimClass = new HashMap<>();
        PRIMITIVE_CLASS_INTERNAL_NAME_MAP.forEach((klass, name) -> internalName2PrimClass.put(name, klass));
        INTERNAL_NAME_2_PRIMITIVE_CLASS = Collections.unmodifiableMap(internalName2PrimClass);
    }

    public static final Map<Class<?>, Class<?>> primitive2wrapper = Map.of(
            byte.class, Byte.class,
            short.class, Short.class,
            int.class, Integer.class,
            long.class, Long.class,
            float.class, Float.class,
            double.class, Double.class,
            boolean.class, Boolean.class,
            char.class, Character.class,
            void.class, Void.class
    );

    public static final Map<Class<?>, Class<?>> wrapper2primitive = Map.of(
            Byte.class, byte.class,
            Short.class, short.class,
            Integer.class, int.class,
            Long.class, long.class,
            Float.class, float.class,
            Double.class, double.class,
            Boolean.class, boolean.class,
            Character.class, char.class,
            Void.class, void.class
    );

    private static final Map<Class<?>, Integer> PRIMITIVE_TYPE_ORDERING_MAP = Map.of(
            byte.class, 5,
            short.class, 4,
            int.class, 3,
            long.class, 2,
            float.class, 1,
            double.class, 0,
            boolean.class, 6,
            char.class, 7,
            void.class, 8
    );

    private static final Set<Class<?>> NUMERIC_PRIMITIVE_TYPES = Set.of(
            byte.class, short.class, int.class, long.class, float.class, double.class
    );

    public static boolean isInstance(Collection<? extends Class<?>> classes, Object object) {
        return Utils.anyMatch(classes, k -> k.isInstance(object));
    }

    public static Unsafe getUnsafe() {
        return UNSAFE;
    }

    public static Type getBoxedType(Type type) {
        if (type instanceof Class<?> klass) {
            return getWrapperClass(klass);
        }
        return type;
    }

    public static void ensureFieldDeclared(Class<?> klass, Field field) {
        Utils.require(field.getDeclaringClass().isAssignableFrom(klass));
    }

    public static Class<?> getWrapperClass(Class<?> klass) {
        if (primitive2wrapper.containsKey(klass)) {
            return primitive2wrapper.get(klass);
        }
        return klass;
    }

    public static String getMethodQualifiedSignature(Class<?> klass, String name, Class<?>... parameterClasses) {
        return getMethodQualifiedSignature(getMethod(klass, name, parameterClasses));
    }

    public static String getConstructorQualifiedSignature(Class<?> klass, Class<?>... parameterClasses) {
        return getConstructorQualifiedSignature(getConstructor(klass, parameterClasses));
    }

    public static boolean isPrimitiveBoxClassName(String name) {
        return Utils.anyMatch(wrapper2primitive.keySet(), k -> k.getName().equals(name));
    }

    public static String getMethodQualifiedSignature(Method method) {
        return method.getDeclaringClass().getName() + "." + getMethodSignature(method);
    }

    public static String getConstructorQualifiedSignature(Constructor<?> constructor) {
        return constructor.getDeclaringClass().getName() + "." + getConstructorSignature(constructor);
    }

    public static String getConstructorSignature(Constructor<?> constructor) {
        return CONSTRUCTOR_NAME + "("
                + Utils.join(
                Arrays.asList(constructor.getParameterTypes()),
                Class::getName,
                ","
        )
                + ")";
    }

    public static String getMethodSignature(Method method) {
        return method.getName() +
                '(' +
                Utils.join(
                        Arrays.asList(method.getParameterTypes()),
                        Class::getName,
                        ","
                ) +
                ')';
    }

    public static String getQualifiedMethodSignature(Method method) {
        return method.getDeclaringClass().getName() + "." + getMethodSignature(method);
    }

    public static boolean isMethodOverrideOf(Method method, Method targetMethod) {
        return targetMethod.getDeclaringClass().isAssignableFrom(method.getDeclaringClass())
                && ReflectionUtils.methodSignatureEquals(method, targetMethod);
    }


    public static boolean methodSignatureEquals(Method method1, Method method2) {
        if (method1.getName().equals(method2.getName())
                && method1.getParameterTypes().length == method2.getParameterTypes().length) {
            for (int i = 0; i < method1.getParameterTypes().length; i++) {
                if (!method1.getParameterTypes()[i].equals(method2.getParameterTypes()[i])) {
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }

    public static Class<?> getPrimitiveClass(String internalName) {
        return Objects.requireNonNull(INTERNAL_NAME_2_PRIMITIVE_CLASS.get(internalName));
    }

    public static Class<?> classForName(String name) {
        try {
            return java.lang.Class.forName(name);
        } catch (ClassNotFoundException e) {
            throw new InternalException("Can not find class: " + name);
        }
    }

    public static Class<?> tryClassForName(String name) {
        try {
            return java.lang.Class.forName(name);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    public static Class<?> getInnerClassRecursively(Class<?> klass, String name) {
        var innerClassName = klass.getName() + "&&" + name;
        var innerClass =
                Utils.find(klass.getDeclaredClasses(), c -> c.getName().equals(innerClassName));
        if (innerClass != null) {
            return innerClass;
        }
        if (klass.getSuperclass() != null && klass.getSuperclass() != Object.class) {
            return getInnerClassRecursively(klass.getSuperclass(), name);
        } else {
            throw new InternalException("Can not find inner class '" + name + "' in class '" + klass.getSimpleName() + "'");
        }
    }

    public static Method getMethod(@NotNull Class<?> klass, String methodName, Class<?>... paramTypes) {
        try {
            return klass.getMethod(methodName, paramTypes);
        } catch (NoSuchMethodException e) {
            throw new InternalException(
                    "Can not find method " + klass.getName() + "." + methodName + "("
                            + Utils.join(paramTypes, Class::getSimpleName) + ")"
            );
        }
    }

    public static void shallowCopy(Object source, Object target) {
        if (!isAncestorClass(source.getClass(), target.getClass())) {
            throw new InternalException("Source class must be ancestor of target class");
        }
        var fields = getDeclaredFieldsRecursively(source.getClass());
        for (Field field : fields) {
            if (!Modifier.isStatic(field.getModifiers())) {
                set(target, field, get(source, field));
            }
        }
    }

    public static boolean isAncestorClass(Class<?> klass1, Class<?> klass2) {
        var t = klass2;
        while (t != null) {
            if (t == klass1) {
                return true;
            }
            t = t.getSuperclass();
        }
        return false;
    }

    public static void forEachField(Class<?> klass, Consumer<Field> action) {
        if (klass.isInterface())
            return;
        var superClass = klass.getSuperclass();
        if (superClass != Object.class)
            forEachField(superClass, action);
        for (Field field : klass.getDeclaredFields()) {
            action.accept(field);
        }
    }

    public static void forEachField(@NotNull Object object, BiConsumer<Field, Object> action) {
        forEachField(object.getClass(), f -> {
            if(!Modifier.isStatic(f.getModifiers()))
               action.accept(f, get(object,f));
        });
    }

    public static Class<?> getCompatibleType(List<Class<?>> classes) {
        Utils.requireNotEmpty(classes);
        Class<?> result = null;
        for (Class<?> klass : classes) {
            if (result == null) {
                result = klass;
            } else {
                result = getCompatibleType(result, klass);
            }
        }
        return result;
    }

    public static Class<?> getCompatibleType(Class<?> class1, Class<?> class2) {
        if (isPrimitiveType(class1) && isPrimitiveType(class2)) {
            return getCompatiblePrimitiveType(class1, class2);
        } else if (class1.isAssignableFrom(class2)) {
            return class1;
        } else if (class2.isAssignableFrom(class1)) {
            return class2;
        } else {
            return Object.class;
        }
    }

    public static Class<?> getCompatiblePrimitiveType(Class<?> klass1, Class<?> klass2) {
        klass1 = unbox(klass1);
        klass2 = unbox(klass2);
        if (primitiveTypeOrder(klass1) > primitiveTypeOrder(klass2)) {
            var temp = klass2;
            klass2 = klass1;
            klass1 = temp;
        }
        if (isNumericPrimitiveType(klass1)) {
            if (klass2 == char.class || klass2 == boolean.class) {
                return Object.class;
            } else {
                return klass1;
            }
        } else {
            return Object.class;
        }
    }

    public static Class<?> getArrayClass(Class<?> klass) {
        String name;
        if (klass.isPrimitive()) {
            name = "[" + PRIMITIVE_CLASS_INTERNAL_NAME_MAP.get(klass);
        } else {
            name = "[L" + klass.getName() + ";";
        }
        return ReflectionUtils.classForName(name);
    }

    public static boolean isNumericPrimitiveType(Class<?> klass) {
        return NUMERIC_PRIMITIVE_TYPES.contains(klass);
    }

    private static int primitiveTypeOrder(Class<?> klass) {
        return Objects.requireNonNull(
                PRIMITIVE_TYPE_ORDERING_MAP.get(klass),
                "class '" + klass.getName() + "' is not a primitive type"
        );
    }

    public static boolean isPrimitiveType(Class<?> klass) {
        return klass.isPrimitive() || wrapper2primitive.containsKey(klass);
    }

    public static Class<?> unbox(Class<?> klass) {
        if (klass.isPrimitive()) {
            return klass;
        }
        return Objects.requireNonNull(
                wrapper2primitive.get(klass),
                "klass '" + klass + "' is not a primitive type"
        );
    }

    public static boolean isPrimitiveWrapper(Class<?> klass) {
        return wrapper2primitive.containsKey(klass);
    }

    public static String getMethodQualifiedName(Method method) {
        return method.getDeclaringClass().getName() + "." + method.getName();
    }

    public static Class<?> getActualReturnType(Method method, List<Class<?>> argumentClasses) {
        Type returnType = method.getGenericReturnType();
        if (returnType instanceof Class<?> klass) {
            return klass;
        }
        Type[] argumentTypes = method.getGenericParameterTypes();
        if (returnType instanceof TypeVariable<?> typeVariable) {
            int i = 0;
            for (Class<?> argumentClass : argumentClasses) {
                if (argumentTypes[i] == typeVariable) {
                    return argumentClass;
                }
                i++;
            }
            return Object.class;
        }
        throw new InternalException("Can not resolve return type '" + returnType + "'. " +
                "Only type variable return type is supported right now.");
    }

    public static Method getMethodByName(@NotNull Class<?> klass, String methodName) {
        return getMethodByName(klass, methodName, true);
    }

    public static Method tryGetStaticMethod(Class<?> klass, String name, Class<?>... paramTypes) {
        try {
            Method method = klass.getMethod(name, paramTypes);
            return Modifier.isStatic(method.getModifiers()) ? method : null;
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    public static MethodHandle unreflect(MethodHandles.Lookup lookup, Method method) {
        try {
            var mh = lookup.unreflect(method);
            var paramCnt = method.getParameterCount();
            mh = mh.asSpreader(
                    Object[].class,
                    Modifier.isStatic(method.getModifiers()) ? paramCnt : paramCnt + 1
            );
            if (method.getReturnType() != Object.class) {
                var adaptType = MethodType.methodType(Object.class, mh.type().parameterArray());
                if (method.getReturnType() == void.class) {
                    var adapter = MethodHandles.dropArguments(
                            MethodHandles.constant(Object.class, null),
                    0, Object[].class);
                    mh = MethodHandles.foldArguments(adapter, mh);
                } else
                    mh = mh.asType(adaptType);
            }
            return mh;
        } catch (Exception e) {
            throw new RuntimeException("Failed to unreflect method " + method, e);
        }
    }

    public static MethodHandle getMethodHandle(MethodHandles.Lookup lookup,
                                                         Class<?> klass,
                                                         String name,
                                                         Class<?> returnClass, List<Class<?>> parameterClasses,
                                                         boolean static_) {
        try {
            var mt = MethodType.methodType(returnClass, parameterClasses.toArray(Class[]::new));
            return static_ ? lookup.findStatic(klass, name, mt) : lookup.findVirtual(klass, name, mt);
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static MethodHandle getMethodHandleWithSpread(MethodHandles.Lookup lookup,
                                                         Class<?> klass,
                                                         String name,
                                                         Class<?> returnClass, List<Class<?>> parameterClasses,
                                                         boolean static_) {
        try {
            var mt = MethodType.methodType(returnClass, parameterClasses.toArray(Class[]::new));
            var mh = static_ ? lookup.findStatic(klass, name, mt) : lookup.findVirtual(klass, name, mt);
            return mh.asSpreader(Object[].class, static_ ? parameterClasses.size() : parameterClasses.size() + 1);
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static Method getMethod(@NotNull Class<?> klass, String name, List<Class<?>> parameterClasses) {
        Class<?>[] paramClasses = new Class<?>[parameterClasses.size()];
        parameterClasses.toArray(paramClasses);
        Class<?> k = klass;
        while (k != null && k != Object.class) {
            Method[] methods = k.getDeclaredMethods();
            for (Method method : methods) {
                if (method.getName().equals(name) &&
                        Arrays.equals(method.getParameterTypes(), paramClasses)) {
                    method.setAccessible(true);
                    return method;
                }
            }
            k = k.getSuperclass();
        }
        throw new RuntimeException(
                "Method " + klass.getName() + "." + name + "(" +
                        Utils.join(paramClasses, Class::getName)
                        + ") not found"
        );
    }

    public static List<Method> getMethods(Class<?> klass, String name) {
        return Utils.filter(Arrays.asList(klass.getMethods()), m -> m.getName().equals(name));
    }

    public static Method getMethodByName(@NotNull Class<?> klass, String methodName, boolean failIfNotFound) {
        Class<?> k = klass;
        while (k != null && k != Object.class) {
            Method[] methods = k.getDeclaredMethods();
            for (Method method : methods) {
                if (method.getName().equals(methodName)) {
                    method.setAccessible(true);
                    return method;
                }
            }
            k = k.getSuperclass();
        }
        if (failIfNotFound) {
            throw new RuntimeException("Method " + klass.getName() + "." + methodName + " not found");
        }
        return null;
    }

    public static <T> T newInstance(Class<T> klass) {
        return allocateInstance(klass);
    }

    public static boolean isCollectionOf(Type type, Class<?> elementType) {
        if (type instanceof ParameterizedType pType) {
            Type rawType = pType.getRawType();
            if (rawType instanceof Class<?> rawClass) {
                return Collection.class.isAssignableFrom(rawClass) && checkTypeArguments(pType, elementType);
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public static String getFieldQualifiedName(Field field) {
        return field.getDeclaringClass().getName() + "." + field.getName();
    }

    public static <T> T allocateInstance(Class<T> klass) {
        try {
            return klass.cast(UNSAFE.allocateInstance(klass));
        } catch (InstantiationException e) {
            throw new InternalException("Fail to allocate instance of class " + klass.getName(), e);
        }
    }

    public static boolean isAllWildCardType(WildcardType wildcardType) {
        return Arrays.equals(new Type[]{Object.class}, wildcardType.getUpperBounds());
    }

    public static boolean checkTypeArguments(ParameterizedType parameterizedType, Class<?>... args) {
        if (parameterizedType.getActualTypeArguments().length == args.length) {
            for (int i = 0; i < args.length; i++) {
                Type typeArg = parameterizedType.getActualTypeArguments()[i];
                if (!(typeArg instanceof Class<?>)) {
                    return false;
                }
                if (!args[i].isAssignableFrom((Class<?>) typeArg)) {
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }

    public static Field getDeclaredField(Class<?> klass, String name) {
        try {
            return klass.getDeclaredField(name);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException("Fail to get field");
        }
    }

    public static List<Field> getDeclaredFieldsRecursively(Class<?> klass) {
        List<Field> result = new ArrayList<>();
        walkClassHierarchyUpwards(klass, k -> {
            result.addAll(Arrays.asList(k.getDeclaredFields()));
            return false;
        });
        result.forEach(Field::trySetAccessible);
        return result;
    }

    public static List<Method> getDeclaredMethodsRecursively(Class<?> klass) {
        return getDeclaredMethodsRecursively(klass, m -> true);
    }

    public static List<Method> getDeclaredMethodsRecursively(Class<?> klass, Predicate<Method> filter) {
        List<Method> result = new ArrayList<>();
        walkClassHierarchyUpwards(klass, k -> {
            result.addAll(Utils.filter(Arrays.asList(k.getDeclaredMethods()), filter));
            return false;
        });
        return result;
    }

    public static Method getDeclaredMethodRecursively(Class<?> klass, Predicate<Method> filter) {
        return Objects.requireNonNull(findDeclaredMethodRecursively(klass, filter));
    }

    @Nullable
    public static Method findDeclaredMethodRecursively(Class<?> klass, Predicate<Method> filter) {
        ValuePlaceholder<Method> holder = new ValuePlaceholder<>();
        walkClassHierarchyUpwards(klass, k -> {
            var methods = k.getDeclaredMethods();
            for (Method method : methods) {
                if (filter.test(method)) {
                    holder.set(method);
                    return true;
                }
            }
            return false;
        });
        return holder.orElseNull();
    }

    public static Method getDeclaredMethod(Class<?> klass, String name, Class<?>...parameterClasses) {
        return getDeclaredMethod(klass, name, List.of(parameterClasses));
    }

    public static Method getDeclaredMethod(Class<?> klass, String name, List<Class<?>> parameterClasses) {
        Class<?>[] paramClassArray = new Class[parameterClasses.size()];
        parameterClasses.toArray(paramClassArray);
        try {
            return klass.getDeclaredMethod(name, paramClassArray);
        } catch (NoSuchMethodException e) {
            throw new InternalException("Can not find method " + klass.getName() + "." + name + "("
                    + Utils.join(parameterClasses, Class::getName, ",") + ")"
            );
        }
    }

    public static List<Class<?>> getDeclaredClassesRecursively(Class<?> klass) {
        List<Class<?>> result = new ArrayList<>();
        walkClassHierarchyUpwards(klass, k -> {
            result.addAll(Arrays.asList(k.getDeclaredClasses()));
            return false;
        });
        return result;
    }

    private static void walkClassHierarchyUpwards(Class<?> klass, Predicate<Class<?>> action) {
        if (action.test(klass)) {
            return;
        }
        if (hasNonObjectSuper(klass)) {
            walkClassHierarchyUpwards(klass.getSuperclass(), action);
        }
    }

    public static Field getDeclaredFieldRecursively(Class<?> klass, String name) {
        try {
            var field = klass.getDeclaredField(name);
            field.trySetAccessible();
            return field;
        } catch (NoSuchFieldException ignored) {
        }
        if (hasNonObjectSuper(klass)) {
            return getDeclaredFieldRecursively(klass.getSuperclass(), name);
        } else {
            throw new InternalException("Can not find field '" + name + "' in class '" + klass.getSimpleName() + "'");
        }
    }

    public static void set(Object object, Field field, Object value) {
        try {
            field.trySetAccessible();
            field.set(object, value);
        } catch (IllegalAccessException | IllegalArgumentException e) {
            throw new RuntimeException("Fail to set field " + field, e);
        }
    }

    public static Object get(Object object, Field field) {
        if (field.equals(getDeclaredField(Enum.class, "name"))) {
            return ((Enum<?>) object).name();
        }
        if (field.equals(getDeclaredField(Enum.class, "ordinal"))) {
            return ((Enum<?>) object).ordinal();
        }
        try {
            field.setAccessible(true);
            return field.get(object);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Fail to get field '" + field.getName() + "'", e);
        }
    }

    public static <T> Constructor<T> getDeclaredConstructor(Class<T> klass, Class<?>... paramTypes) {
        try {
            return klass.getDeclaredConstructor(paramTypes);
        } catch (NoSuchMethodException e) {
            throw new InternalException("Fail to get constructor for type: " + klass.getName() +
                    " with parameter types: " + Arrays.toString(paramTypes));
        }
    }

    public static Type getType(Object object) {
        return switch (object) {
            case RuntimeGeneric runtimeGeneric -> runtimeGeneric.getGenericType();
            case Enum<?> e -> getEnumClass(e);
            default -> object.getClass();
        };
    }


    public static List<Field> getDeclaredStaticFields(Class<?> klass, Predicate<Field> filter) {
        List<Field> results = new ArrayList<>();
        for (Field declaredField : klass.getDeclaredFields()) {
            if (Modifier.isStatic(declaredField.getModifiers()) && filter.test(declaredField)) {
                results.add(declaredField);
            }
        }
        return results;
    }

    public static Field getField(final Class<?> klass, String name) {
        return Objects.requireNonNull(findField(klass, name),
                () -> "Cannot find field '" + name + "' in class" + klass.getName());
    }

    public static @Nullable Field findField(Class<?> klass, String name) {
        var key = new FieldKey(klass, name);
        var existing = fieldMap.get(key);
        if(existing != null)
            return existing;
        try {
            Field field = klass.getDeclaredField(name);
            trySetAccessible(field);
            fieldMap.put(key, field);
            return field;
        } catch (NoSuchFieldException e) {
            if (klass.getSuperclass() != null) {
                return findField(klass.getSuperclass(), name);
            } else {
                return null;
            }
        }

    }


    public static Field getStaticField(Class<?> klass, String name) {
        try {
            var field = klass.getField(name);
            if (Modifier.isStatic(field.getModifiers())) {
                return field;
            }
        } catch (NoSuchFieldException ignore) {
        }
        throw new InternalException("Can not find static field '" + name + "' in class '" + klass.getName() + "'");
    }

    public static int getIntField(Field field, @javax.annotation.Nullable Object object) {
        try {
            return field.getInt(object);
        }
        catch (IllegalAccessException e) {
            throw new InternalException("Fail to invoke getInt on " + field + " with object: " + object);
        }
    }

    public static List<Field> getStaticFields(Class<?> klass) {
        return Utils.filter(
                Arrays.asList(klass.getFields()),
                f -> Modifier.isStatic(f.getModifiers())
        );
    }

    public static List<Method> getStaticMethodsRecursively(Class<?> klass, Class<?> currentClass) {
        IntPredicate modifierFilter = mod -> {
            if (!Modifier.isStatic(mod)) {
                return false;
            }
            if (klass.getPackage().equals(currentClass.getPackage())) {
                return !Modifier.isPrivate(mod);
            }
            if (klass.isAssignableFrom(currentClass)) {
                return Modifier.isPublic(mod) || Modifier.isProtected(mod);
            } else {
                return Modifier.isPublic(mod);
            }
        };
        return getMethodsRecursively(klass, modifierFilter);
    }

    public static List<Method> getMethodsRecursively(Class<?> klass, IntPredicate modifierFilter) {
        List<Method> result = new ArrayList<>();
        getMethodsRecursively0(klass, modifierFilter, result);
        return result;
    }

    private static void getMethodsRecursively0(Class<?> klass, IntPredicate modifierFilter, List<Method> result) {
        result.addAll(
                Utils.filter(
                        Arrays.asList(klass.getDeclaredMethods()),
                        m -> modifierFilter.test(m.getModifiers())
                )
        );
        if (hasNonObjectSuper(klass)) {
            getMethodsRecursively0(klass.getSuperclass(), modifierFilter, result);
        }
    }

    public static boolean hasNonObjectSuper(Class<?> klass) {
        return klass.getSuperclass() != null && klass.getSuperclass() != Object.class;
    }

    private static boolean isPackagePrivate(int modifier) {
        return (modifier & (Modifier.PRIVATE | Modifier.PUBLIC | Modifier.PROTECTED)) == 0;
    }

    private static void getAllDeclaredStaticFields(Class<?> klass, List<Field> result) {
        result.addAll(
                Utils.filter(
                        Arrays.asList(klass.getDeclaredFields()),
                        f -> Modifier.isStatic(f.getModifiers())
                )
        );
        if (klass.getSuperclass() != null && klass.getSuperclass() != Object.class) {
            getAllDeclaredStaticFields(klass.getSuperclass(), result);
        }
    }

    public static Field tryGetInstanceField(Class<?> klass, String name) {
        try {
            Field field = klass.getDeclaredField(name);
            if (!Modifier.isStatic(field.getModifiers())) {
                return field;
            }
            if (klass.getSuperclass() != null && klass.getSuperclass() != Object.class) {
                return tryGetInstanceField(klass.getSuperclass(), name);
            } else {
                return null;
            }
        } catch (NoSuchFieldException e) {
            return null;
        }
    }

    private static Field trySetAccessible(Field field) {
        if (field.getDeclaringClass().getName().startsWith(META_VM_PKG)) {
            field.setAccessible(true);
        }
        return field;
    }

    public static Field getDeclaredFieldByName(Class<?> klass, String name) {
        for (Field declaredField : klass.getDeclaredFields()) {
            if (declaredField.getName().equals(name)) {
                return declaredField;
            }
        }
        throw new InternalException("Can not find a declared field for meta field name '" + name + "' " +
                "in class '" + klass.getName() + "'");
    }

    public static Object getFieldValue(Object object, Field field) {
        if (field.equals(getField(Enum.class, "name"))) {
            return ((Enum<?>) object).name();
        }
        if (field.equals(getField(Enum.class, "ordinal"))) {
            return ((Enum<?>) object).ordinal();
        }
        try {
            return field.get(object);
        } catch (IllegalAccessException e) {
            throw new InternalException("Fail to get field value, field: " + field + ", object: " + object, e);
        }
    }

    public static List<Field> getInstanceFields(Class<?> klass) {
        return getInstanceFields(klass, null);
    }

    @SuppressWarnings("unused")
    public static List<Field> getDeclaredInstanceFields(Class<?> klass) {
        return getDeclaredInstanceFields(klass, f -> true);
    }

    public static List<Field> getDeclaredPersistentFields(Class<?> klass) {
        return getDeclaredInstanceFields(klass, f -> !Modifier.isTransient(f.getModifiers()));
    }

    public static List<Field> getDeclaredInstanceFields(Class<?> klass, Predicate<Field> filter) {
        List<Field> allFields = new ArrayList<>();
        for (Field declaredField : klass.getDeclaredFields()) {
            if (!Modifier.isStatic(declaredField.getModifiers()) && filter.test(declaredField)) {
                declaredField.setAccessible(true);
                allFields.add(declaredField);
            }
        }
        return allFields;
    }

    public static String getSimpleTypeName(Type type) {
        return getSimpleTypeName0(type, new IdentitySet<>());
    }

    private static String getSimpleTypeName0(Type type, IdentitySet<Type> visited) {
        if (visited.contains(type)) {
            throw new InternalException("Circular reference");
        }
        visited.add(type);
        if (type instanceof Class<?> klass) {
            return klass.getSimpleName();
        }
        if (type instanceof ParameterizedType parameterizedType) {
            Class<?> rawClass = (Class<?>) parameterizedType.getRawType();
            return rawClass.getSimpleName() + "<" +
                    Utils.join(
                            parameterizedType.getActualTypeArguments(),
                            t -> getSimpleTypeName0(t, visited)
                    ) + ">";
        } else {
            throw new InternalException("Can not erase type " + type);
        }
    }

    public static Class<?> eraseToClass(Type type) {
        if (type instanceof Class<?> klass) {
            return klass;
        }
        if (type instanceof ParameterizedType parameterizedType) {
            return (Class<?>) parameterizedType.getRawType();
        }
        return Object.class;
    }

    public static Class<?> getEnumClass(Enum<?> enumConstant) {
        var klass = enumConstant.getClass();
        if (klass.isEnum())
            return klass;
        else
            return klass.getSuperclass();
    }

    public static Type eraseType(Type type) {
        return eraseType0(type, new IdentitySet<>());
    }

    private static Type eraseType0(Type type, IdentitySet<Type> visited) {
        if (visited.contains(type))
            throw new InternalException("Circular reference");
        visited.add(type);
        return switch (type) {
            case Class<?> aClass -> type;
            case TypeVariable<?> typeVariable -> type;
            case WildcardType wildcardType -> eraseType0(wildcardType.getUpperBounds()[0], visited);
            case GenericArrayType genericArrayType -> Object.class.arrayType();
            case ParameterizedType parameterizedType -> {
                Class<?> rawClass = (Class<?>) parameterizedType.getRawType();
                if (RuntimeGeneric.class.isAssignableFrom(rawClass) || List.class.isAssignableFrom(rawClass)) {
                    yield ParameterizedTypeImpl.create(
                            rawClass,
                            Utils.map(
                                    parameterizedType.getActualTypeArguments(),
                                    t -> eraseType0(t, visited)
                            )
                    );
                } else
                    yield rawClass;
            }
            case null, default -> throw new InternalException("Can not erase type " + type);
        };
    }

    public static JavaSubstitutor resolveGenerics(Type type) {
        ResolveVisitor visitor = new ResolveVisitor();
        visitor.visitType(type);
        return visitor.getSubstitutor();
    }

    private static class ResolveVisitor extends JavaTypeVisitor {

        private JavaSubstitutor substitutor = JavaSubstitutorImpl.EMPTY;

        @Override
        public void visitClass(Class<?> klass) {
            if (klass.getGenericSuperclass() != null) {
                visitType(substitutor.substitute(klass.getGenericSuperclass()));
            }
            for (Type genericInterface : klass.getGenericInterfaces()) {
                visitType(substitutor.substitute(genericInterface));
            }
        }

        @Override
        public void visitParameterizedType(ParameterizedType pType) {
            var klass = (Class<?>) pType.getRawType();
            Map<TypeVariable<?>, Type> map = new HashMap<>();
            Utils.biForEach(
                    klass.getTypeParameters(),
                    pType.getActualTypeArguments(),
                    map::put
            );
            substitutor = substitutor.merge(map);
            visitType(pType.getRawType());
        }

        public JavaSubstitutor getSubstitutor() {
            return substitutor;
        }
    }

    public static Type evaluateFieldType(Type declaringType, Type fieldType) {
        if (fieldType instanceof Class<?> klass) {
            return klass;
        }
        if (fieldType instanceof WildcardType wildcardType) {
            return wildcardType;
        }
        if (fieldType instanceof ParameterizedType pType) {
            return ParameterizedTypeImpl.create(
                    (Class<?>) pType.getRawType(),
                    Utils.map(
                            pType.getActualTypeArguments(),
                            typeArg -> evaluateFieldType(declaringType, typeArg)
                    )
            );
        }
        if (fieldType instanceof TypeVariable<?> typeVariable) {
            if (declaringType instanceof ParameterizedType pType) {
                return evaluateTypeVariable(pType, typeVariable);
            } else {
                return typeVariable;
            }
        }
        throw new InternalException("Can not evaluate member type " + fieldType + " defined in type " + declaringType);
    }

    public static List<Field> getDeclaredRawFields(Type type) {
        if (type instanceof Class<?> klass) {
            return Arrays.asList(klass.getDeclaredFields());
        }
        if (type instanceof ParameterizedType pType) {
            return getDeclaredRawFields(pType.getRawType());
        }
        return List.of();
    }

    private static Type evaluateTypeVariable(ParameterizedType declaringType, TypeVariable<?> typeVariable) {
        Class<?> rawClass = (Class<?>) declaringType.getRawType();
        List<TypeVariable<?>> typeParams = Arrays.asList(rawClass.getTypeParameters());
        int idx = typeParams.indexOf(typeVariable);
        if (idx >= 0) {
            return declaringType.getActualTypeArguments()[idx];
        }
        throw new RuntimeException(typeVariable + " is not defined in " + declaringType);
    }

    public static Class<?> getRawClass(Type type) {
        return switch (type) {
            case Class<?> klass -> klass;
            case ParameterizedType parameterizedType -> getRawClass(parameterizedType.getRawType());
            case WildcardType wildcardType -> getRawClass(wildcardType.getUpperBounds()[0]);
            case TypeVariable<?> typeVariable -> getRawClass(typeVariable.getBounds()[0]);
            default -> throw new IllegalStateException("Unexpected value: " + type);
        };
    }

    public static List<Field> getInstanceFields(Class<?> klass, Class<? extends Annotation> annotationClass) {
        List<Field> allFields = new ArrayList<>();
        while (klass != Object.class && klass != null) {
            for (Field declaredField : klass.getDeclaredFields()) {
                if (!Modifier.isStatic(declaredField.getModifiers())
                        && (annotationClass == null || declaredField.isAnnotationPresent(annotationClass))) {
                    declaredField.trySetAccessible();
                    allFields.add(declaredField);
                }
            }
            klass = klass.getSuperclass();
        }
        return allFields;
    }

    public static Field getField(RecordComponent recordComponent) {
        return getField(recordComponent.getDeclaringRecord(), recordComponent.getName());
    }

    public static <T> Constructor<T> getConstructor(Class<T> klass, Class<?>... paramTypes) {
        try {
            return klass.getConstructor(paramTypes);
        } catch (Exception e) {
            throw new RuntimeException("Constructor not found", e);
        }
    }

    public static <T> Constructor<T> getConstructorIfPresent(Class<T> klass, Class<?>... paramTypes) {
        try {
            return klass.getConstructor(paramTypes);
        } catch (Exception e) {
            return null;
        }
    }

    @SuppressWarnings("unused")
    public static <T> T invokeConstructor(Constructor<T> constructor, Object... args) {
        try {
            constructor.setAccessible(true);
            return constructor.newInstance(args);
        } catch (Exception e) {
            throw new InternalException("Fail to create instance by constructor: " + constructor, e);
        }
    }

    public static <T> T newInstance(Constructor<T> constructor, Object... arguments) {
        try {
            return constructor.newInstance(arguments);
        } catch (Exception e) {
            throw new RuntimeException("Fail to create instance", e);
        }
    }

    public static Object invoke(Object object, Method method, Object... arguments) {
        try {
            return method.invoke(object, arguments);
        } catch (Exception e) {
            throw new RuntimeException("Fail to invoke method: " + getMethodQualifiedName(method), e);
        }
    }

    public static boolean isAnnotatedWithNullable(AnnotatedElement element) {
        return element.isAnnotationPresent(javax.annotation.Nullable.class)
                || element.isAnnotationPresent(Nullable.class);

    }

    public static String dumpObject(Object object) {
        if(object == null)
            return "null";
        Map<String, Object> values = new HashMap<>();
        forEachField(object, (f, v) -> values.put(f.getName(), Objects.toString(v)));
        return Utils.toPrettyJsonString(values);
    }

    public static boolean isPrimitiveType(Type type) {
        return type instanceof Class<?> klass && klass.isPrimitive();
    }

}
