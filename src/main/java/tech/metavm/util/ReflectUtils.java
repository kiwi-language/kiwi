package tech.metavm.util;

import org.jetbrains.annotations.NotNull;
import org.reflections.Reflections;
import sun.misc.Unsafe;
import tech.metavm.entity.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;
import java.util.function.Predicate;

public class ReflectUtils {

    public static final Unsafe UNSAFE;

    public static final Field ENUM_NAME_FIELD = getField(Enum.class, "name");

    public static final Field ENUM_ORDINAL_FIELD = getField(Enum.class, "ordinal");

    static {
        Field unsafeField = getDeclaredField(Unsafe.class, "theUnsafe");
        unsafeField.setAccessible(true);
        UNSAFE = (Unsafe) get(null, unsafeField);
    }

    public static final Map<Class<?>, Class<?>> PRIMITIVE_CLASSES = Map.of(
            byte.class,  Byte.class,
            short.class, Short.class,
            int.class, Integer.class,
            long.class, Long.class,
            float.class, Float.class,
            double.class, Double.class,
            boolean.class, Boolean.class,
            char.class, Character.class
    );

    public static Unsafe getUnsafe() {
        return UNSAFE;
    }

    public static Set<Class<?>> getModelClasses() {
        Reflections reflections = new Reflections("tech.metavm");
        Set<Class<? extends Entity>> entitySubTypes = reflections.getSubTypesOf(Entity.class);
        Set<Class<?>> entityTypes = reflections.getTypesAnnotatedWith(EntityType.class);
        Set<Class<?>> valueTypes = reflections.getTypesAnnotatedWith(ValueType.class);
        return NncUtils.mergeSets(entitySubTypes, entityTypes, valueTypes);
    }

    public static Type getBoxedType(Type type) {
        if(type instanceof Class<?> klass) {
            return getBoxedClass(klass);
        }
        return type;
    }

    public static Class<?> getBoxedClass(Class<?> klass) {
        if(PRIMITIVE_CLASSES.containsKey(klass)) {
            return PRIMITIVE_CLASSES.get(klass);
        }
        return klass;
    }

    public static Class<?> classForName(String name) {
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException e) {
            throw new InternalException("Can not find class: " + name);
        }
    }

    public static Method getMethod(@NotNull Class<?> klass, String methodName, Class<?>...paramTypes) {
        try {
            return klass.getMethod(methodName, paramTypes);
        } catch (NoSuchMethodException e) {
            throw new InternalException(
                    "Can not find method " + klass.getName() + "." + methodName + "("
                    + NncUtils.join(paramTypes, Class::getSimpleName) + ")"
            );
        }
    }

    public static String getMethodQualifiedName(Method method) {
        return method.getDeclaringClass().getName() + "." + method.getName();
    }

    public static String getMetaFieldName(Class<?> klass, String javaFieldName) {
        return getMetaFieldName(getField(klass, javaFieldName));
    }

    public static String getMetaFieldName(Field javaField) {
        if(ENUM_NAME_FIELD.equals(javaField)) {
            return "名称";
        }
        if(ENUM_ORDINAL_FIELD.equals(javaField)) {
            return "序号";
        }
        EntityField entityField = javaField.getAnnotation(EntityField.class);
        ChildEntity childEntity = javaField.getAnnotation(ChildEntity.class);
        return NncUtils.firstNonNull(
                NncUtils.get(entityField, EntityField::value),
                NncUtils.get(childEntity, ChildEntity::value),
                javaField.getName()
        );
    }

    public static String getMetaTypeName(Class<?> javaType) {
        EntityType entityType = javaType.getAnnotation(EntityType.class);
        ValueType valueType = javaType.getAnnotation(ValueType.class);
        return NncUtils.firstNonNull(
                NncUtils.get(entityType, EntityType::value),
                NncUtils.get(valueType, ValueType::value),
                javaType.getSimpleName()
        );
    }

    public static Method tryGetMethodByName(Class<?> klass, String methodName) {
        return getMethodByName(klass, methodName, false);
    }

    public static Class<?> getActualReturnType(Method method, List<Class<?>> argumentClasses) {
        Type returnType = method.getGenericReturnType();
        if(returnType instanceof Class<?> klass) {
            return klass;
        }
        Type[] argumentTypes = method.getGenericParameterTypes();
        if(returnType instanceof TypeVariable<?> typeVariable) {
            int i = 0;
            for (Class<?> argumentClass : argumentClasses) {
                if(argumentTypes[i] == typeVariable) {
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

    public static Method tryGetStaticMethod(Class<?> klass, String name, Class<?>...paramTypes) {
        try {
            Method method =  klass.getMethod(name, paramTypes);
            return Modifier.isStatic(method.getModifiers()) ? method : null;
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    public static Method getMethod(@NotNull Class<?> klass, String name, List<Class<?>> parameterClasses) {
        Class<?>[] paramClasses = new Class<?>[parameterClasses.size()];
        parameterClasses.toArray(paramClasses);
        Class<?> k = klass;
        while(k != null && k != Object.class) {
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
                        NncUtils.join(paramClasses, Class::getName)
                        +") not found"
        );
    }

    public static Method getMethodByName(@NotNull Class<?> klass, String methodName, boolean failIfNotFound) {
        Class<?> k = klass;
        while(k != null && k != Object.class) {
            Method[] methods = k.getDeclaredMethods();
            for (Method method : methods) {
                if (method.getName().equals(methodName)) {
                    method.setAccessible(true);
                    return method;
                }
            }
            k = k.getSuperclass();
        }
        if(failIfNotFound) {
            throw new RuntimeException("Method " + klass.getName() + "." + methodName + " not found");
        }
        return null;
    }

    public static Object newInstance(Class<?> klass) {
        try {
            return UNSAFE.allocateInstance(klass);
        } catch (InstantiationException e) {
            throw new RuntimeException("Fail to create instance of " + klass.getName(), e);
        }
    }

    public static boolean isCollectionOf(Type type, Class<?> elementType) {
        if(type instanceof ParameterizedType pType) {
            Type rawType = pType.getRawType();
            if(rawType instanceof Class<?> rawClass) {
                return Collection.class.isAssignableFrom(rawClass) && checkTypeArguments(pType, elementType);
            }
            else {
                return false;
            }
        }
        else {
            return false;
        }
    }

    public static String getFieldQualifiedName(Field field) {
        return field.getDeclaringClass().getName() + "." + field.getName();
    }

    public static boolean isIndexDefField(Field field) {
        return Modifier.isStatic(field.getModifiers()) && field.getType() == IndexDef.class;
    }

    public static <T> T allocateInstance(Class<T> klass) {
        try {
            return klass.cast(UNSAFE.allocateInstance(klass));
        } catch (InstantiationException e) {
            throw new InternalException("Fail to allocate instance of class " + klass.getName());
        }
    }

    public static boolean isAllWildCardType(WildcardType wildcardType) {
        return Arrays.equals(new Type[]{Object.class}, wildcardType.getUpperBounds());
    }

    public static boolean checkTypeArguments(ParameterizedType parameterizedType, Class<?>...args) {
        if(parameterizedType.getActualTypeArguments().length == args.length) {
            for(int i = 0; i < args.length; i++) {
                Type typeArg = parameterizedType.getActualTypeArguments()[i];
                if(! (typeArg instanceof Class<?>)) {
                    return false;
                }
                if(!args[i].isAssignableFrom((Class<?>)typeArg)) {
                    return false;
                }
            }
            return true;
        }
        else {
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

    public static void set(Object object, Field field, Object value) {
        try {
            field.set(object, value);
        } catch (IllegalAccessException|IllegalArgumentException e) {
            throw new RuntimeException("Fail to set field", e);
        }
    }

    public static Object get(Object object, Field field) {
        if(field.equals(getDeclaredField(Enum.class, "name"))) {
            return ((Enum<?>) object).name();
        }
        if(field.equals(getDeclaredField(Enum.class, "ordinal"))) {
            return ((Enum<?>) object).ordinal();
        }
        try {
            return field.get(object);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Fail to set field", e);
        }
    }

    public static <T> Constructor<T> getDeclaredConstructor(Class<T> klass, Class<?>...paramTypes) {
        try {
            return klass.getDeclaredConstructor(paramTypes);
        } catch (NoSuchMethodException e) {
            throw new InternalException("Fail to get constructor for type: " + klass.getName() +
                    " with parameter types: " + Arrays.toString(paramTypes));
        }
    }

    public static Type getType(Object object) {
        if(object instanceof RuntimeGeneric runtimeGeneric) {
            return runtimeGeneric.getGenericType();
        }
        else {
            return object.getClass();
        }
    }

    public static List<Field> getIndexDefFields(Class<?> klass) {
        return getDeclaredStaticFields(
                klass,
                f -> f.getType() == IndexDef.class
        );
    }

    public static List<Field> getDeclaredStaticFields(Class<?> klass, Predicate<Field> filter) {
        List<Field> results = new ArrayList<>();
        for (Field declaredField : klass.getDeclaredFields()) {
            if(Modifier.isStatic(declaredField.getModifiers()) && filter.test(declaredField)) {
                results.add(declaredField);
            }
        }
        return results;
    }

    public static Field getField(Class<?> klass, String name) {
        try {
            Field field = klass.getDeclaredField(name);
            if(klass.getName().startsWith(Constants.META_VM_PKG)) {
                field.setAccessible(true);
            }
            return field;
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    public static Field getDeclaredFieldByMetaFieldName(Class<?> klass, String metaFieldName) {
        for (Field declaredField : klass.getDeclaredFields()) {
            if(getMetaFieldName(declaredField).equals(metaFieldName)) {
                return declaredField;
            }
        }
        throw new InternalException("Can not find a declared field for meta field name '" + metaFieldName + "' " +
                        "in class '" + klass.getName() + "'");
    }

    public static Set<ModelAndPath> getReachableObjects(Collection<Object> objects,
                                                  Predicate<Object> filter,
                                                  boolean ignoreTransientFields) {
        Set<ModelAndPath> result = new IdentitySet<>();
        Set<Object> visited = new IdentitySet<>();
        for (Object object : objects) {
            getReachableObjects0(object, filter, ignoreTransientFields, new LinkedList<>(), visited, result);
        }
        return result;
    }

    private static void getReachableObjects0(Object object,
                                             Predicate<Object> filter,
                                             boolean ignoreTransientFields,
                                             LinkedList<String> path,
                                             Set<Object> visited,
                                             Set<ModelAndPath> result) {
        if(object == null || ValueUtil.isPrimitive(object) || visited.contains(object) || !filter.test(object)) {
            return;
        }
        result.add(new ModelAndPath(object, NncUtils.join(path, Objects::toString, ".")));
        visited.add(object);
        if(object instanceof Collection<?> collection) {
            int index = 0;
            for (Object item : collection) {
                path.addLast(index + "");
                getReachableObjects0(item, filter, ignoreTransientFields, path, visited, result);
                path.removeLast();
                index++;
            }
        }
        else {
            for (EntityProp prop : DescStore.get(object.getClass()).getProps()) {
                if (prop.isAccessible() && !(ignoreTransientFields && prop.isTransient())) {
                    path.addLast(prop.getName());
                    getReachableObjects0(prop.get(object), filter, ignoreTransientFields, path, visited, result);
                    path.removeLast();
                }
            }
        }
    }

    public static Map<Object, List<Reference>> buildInvertedIndex(Collection<Object> objects, Predicate<Object> filter) {
        List<Reference> references = extractReferences(objects, filter);
        Map<Object, List<Reference>> index = new IdentityHashMap<>();
        for (Reference reference : references) {
            index.computeIfAbsent(reference.target(), t -> new ArrayList<>()).add(reference);
        }
        return index;
    }

    public static List<Reference> extractReferences(Collection<Object> objects, Predicate<Object> filter) {
        List<Reference> result = new LinkedList<>();
        Set<Object> visited = new IdentitySet<>();
        for (Object object : objects) {
            extractReferences0(object, visited, filter, result);
        }
        return result;
    }

    private static void extractReferences0(Object object, Set<Object> visited, Predicate<Object> filter, List<Reference> result) {
        if(object == null || ValueUtil.isPrimitive(object) || ValueUtil.isJavaType(object)
                || !filter.test(object) || visited.contains(object)) {
            return;
        }
        visited.add(object);
        for (EntityProp prop : DescStore.get(object.getClass()).getProps()) {
            if (prop.isAccessible()) {
                Object fieldValue = prop.get(object);
                if (fieldValue != null && !ValueUtil.isPrimitive(fieldValue) && !ValueUtil.isJavaType(fieldValue)) {
                    result.add(new Reference(object, prop.getField().getName(), fieldValue));
                    extractReferences0(fieldValue, visited, filter, result);
                }
            }
        }
    }

    private static boolean isReferenceTraversalTarget(Object object, Predicate<Object> filter, Set<Object> visited) {
        return object != null && !ValueUtil.isPrimitive(object) && !ValueUtil.isJavaType(object)
                && filter.test(object) && !visited.contains(object);
    }

    public static Object getFieldValue(Object object, Field field) {
        if(field.equals(getField(Enum.class, "name"))) {
            return ((Enum<?>) object).name();
        }
        if(field.equals(getField(Enum.class, "ordinal"))) {
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
            if(!Modifier.isStatic(declaredField.getModifiers()) && filter.test(declaredField)) {
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
        if(visited.contains(type)) {
            throw new InternalException("Circular reference");
        }
        visited.add(type);
        if(type instanceof Class<?> klass) {
            return klass.getSimpleName();
        }
        if(type instanceof ParameterizedType parameterizedType) {
            Class<?> rawClass = (Class<?>) parameterizedType.getRawType();
                return rawClass.getSimpleName() + "<" +
                        NncUtils.join(
                                parameterizedType.getActualTypeArguments(),
                                t -> getSimpleTypeName0(t, visited)
                        ) + ">";
        }
        else {
            throw new InternalException("Can not erase type " + type);
        }
    }

    public static Type eraseType(Type type) {
        return eraseType0(type, new IdentitySet<>());
    }

    private static Type eraseType0(Type type, IdentitySet<Type> visited) {
        if(visited.contains(type)) {
            throw new InternalException("Circular reference");
        }
        visited.add(type);
        if(type instanceof Class<?>) {
            return type;
        }
        if(type instanceof ParameterizedType parameterizedType) {
            Class<?> rawClass = (Class<?>) parameterizedType.getRawType();
            if(RuntimeGeneric.class.isAssignableFrom(rawClass)) {
                return ParameterizedTypeImpl.create(
                        rawClass,
                        NncUtils.map(
                                parameterizedType.getActualTypeArguments(),
                                t -> eraseType0(t, visited)
                        )
                );
            }
            else {
                return rawClass;
            }
        }
        else {
            throw new InternalException("Can not erase type " + type);
        }
    }

    public static Class<?> getRawClass(Type type) {
        if(type instanceof Class<?> klass) {
            return klass;
        }
        if(type instanceof ParameterizedType parameterizedType) {
            return getRawClass(parameterizedType.getRawType());
        }
        throw new InternalException("Can not get raw type for: " + type);
    }

    public static List<Field> getInstanceFields(Class<?> klass, Class<? extends Annotation> annotationClass) {
        List<Field> allFields = new ArrayList<>();
        while(klass != Object.class && klass != null) {
            for (Field declaredField : klass.getDeclaredFields()) {
                if(!Modifier.isStatic(declaredField.getModifiers())
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

    public static <T> Constructor<T> getConstructorIfPresent(Class<T> klass, Class<?>...paramTypes) {
        try {
            return klass.getConstructor(paramTypes);
        } catch (Exception e) {
            return null;
        }
    }

    @SuppressWarnings("unused")
    public static <T> T invokeConstructor(Constructor<T> constructor, Object...args) {
        try {
            constructor.setAccessible(true);
            return constructor.newInstance(args);
        } catch (Exception e) {
            throw new InternalException("Fail to create instance by constructor: " + constructor);
        }
    }

    public static <T> T newInstance(Constructor<T> constructor, Object...arguments) {
        try {
            return constructor.newInstance(arguments);
        } catch (Exception e) {
            throw new RuntimeException("Fail to create instance", e);
        }
    }

    public static Object invoke(Object object, Method method, Object...argiments) {
        try {
            return method.invoke(object, argiments);
        } catch (Exception e) {
            throw new RuntimeException("Fail to invoke method", e);
        }
    }

}
