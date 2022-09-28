package tech.metavm.util;

import sun.misc.Unsafe;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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

    public static Method getMethodByName(Class<?> klass, String methodName) {
        Method[] methods = klass.getMethods();
        for (Method method : methods) {
            if(method.getName().equals(methodName)) {
                return method;
            }
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
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Fail to set field", e);
        }
    }

    public static Object get(Object object, Field field) {
        try {
            return field.get(object);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Fail to set field", e);
        }
    }

    public static Field getField(Class<?> klass, String name) {
        try {
            Field field = klass.getDeclaredField(name);
            field.setAccessible(true);
            return field;
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    public static List<Field> getAllFields(Class<?> klass) {
        List<Field> allFields = new ArrayList<>();
        while(klass != Object.class && klass != null) {
            for (Field declaredField : klass.getDeclaredFields()) {
                if(!Modifier.isStatic(declaredField.getModifiers())) {
                    declaredField.setAccessible(true);
                    allFields.add(declaredField);
                }
            }
            klass = klass.getSuperclass();
        }
        return allFields;
    }

    public static <T> Constructor<T> getConstructor(Class<T> cls, Class<?>... argTypes) {
        try {
            return cls.getConstructor(argTypes);
        } catch (Exception e) {
            throw new RuntimeException("Constructor not found", e);
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
