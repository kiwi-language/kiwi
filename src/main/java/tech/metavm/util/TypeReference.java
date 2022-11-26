package tech.metavm.util;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Objects;

public abstract class TypeReference<T> {

    public static <T> TypeReference<T> of(Class<T> klass) {
        TypeReference<T>  typeRef = new TypeReference<T>() {};
        typeRef.type = klass;
        return typeRef;
    }

    private Type type;

    private TypeReference(Class<T> klass) {
        this.type = klass;
    }

    protected TypeReference() {
        type = createType();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TypeReference<?> that = (TypeReference<?>) o;
        return Objects.equals(type, that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type);
    }

    public Type getGenericType() {
        return type;
    }

    private Type createType() {
        Type superClass = this.getClass().getGenericSuperclass();
        if(superClass instanceof ParameterizedType parameterizedType) {
            if(parameterizedType.getRawType() == TypeReference.class) {
                return parameterizedType.getActualTypeArguments()[0];
            }
        }
        throw new InternalException("Using type reference as raw type is not allowed");
    }

    @SuppressWarnings("unchecked")
    public Class<T> getType() {
        return (Class<T>) ReflectUtils.getRawTypeRecursively(type);
    }

    public T cast(Object object) {
        return getType().cast(object);
    }

}
