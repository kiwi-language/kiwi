package tech.metavm.util;

import org.jetbrains.annotations.NotNull;
import sun.misc.Unsafe;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

public class ReflectUtils {

    public static final Unsafe UNSAFE;

    static {
        Field unsafeField = getDeclaredField(Unsafe.class, "theUnsafe");
        unsafeField.setAccessible(true);
        UNSAFE = (Unsafe) get(null, unsafeField);
    }

    public static Unsafe getUnsafe() {
        return UNSAFE;
    }

    public static Method getMethodByName(@NotNull Class<?> klass, String methodName) {
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
        throw new RuntimeException("Method " + klass.getName() + "." + methodName + " not found");
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

    public static <T> T allocateInstance(Class<T> klass) {
        try {
            return klass.cast(UNSAFE.allocateInstance(klass));
        } catch (InstantiationException e) {
            throw new InternalException("Fail to allocate instance of class " + klass.getName());
        }
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

    public static Class<?> getRawTypeRecursively(Type type) {
        if(type instanceof Class<?> klass) {
            return klass;
        }
        if(type instanceof ParameterizedType parameterizedType) {
            return getRawTypeRecursively(parameterizedType.getRawType());
        }
        throw new InternalException("Can not get raw type for: " + type);
    }

    public static List<Field> getInstanceFields(Class<?> klass, Class<? extends Annotation> annotationClass) {
        List<Field> allFields = new ArrayList<>();
        while(klass != Object.class && klass != null) {
            for (Field declaredField : klass.getDeclaredFields()) {
                if(!Modifier.isStatic(declaredField.getModifiers())
                        && (annotationClass == null || declaredField.isAnnotationPresent(annotationClass))) {
                    declaredField.setAccessible(true);
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

    public static <T> Constructor<T> getConstructor(Class<T> cls, Class<?>... argTypes) {
        try {
            return cls.getConstructor(argTypes);
        } catch (Exception e) {
            throw new RuntimeException("Constructor not found", e);
        }
    }

    @SuppressWarnings("unused")
    public static <T> T invokeConstructor(Constructor<T> constructor, Object...args) {
        try {
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

    public static Object invoke(Object object, Method method) {
        try {
            return method.invoke(object);
        } catch (Exception e) {
            throw new RuntimeException("Fail to invoke method", e);
        }
    }

}
