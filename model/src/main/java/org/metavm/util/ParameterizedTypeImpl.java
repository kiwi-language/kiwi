package org.metavm.util;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

public class ParameterizedTypeImpl implements ParameterizedType {

    public static ParameterizedType create(Class<?> rawType, Type...actualTypeArguments) {
        return new ParameterizedTypeImpl(null, rawType, actualTypeArguments);
    }

    public static ParameterizedType create(Class<?> rawType, List<Type> actualTypeArguments) {
        Type[] typeArgs = new Type[actualTypeArguments.size()];
        actualTypeArguments.toArray(typeArgs);
        return create(rawType, typeArgs);
    }

    private final Type ownerType;
    private final Class<?> rawType;
    private final Type[] actualTypeArguments;

    public ParameterizedTypeImpl(Type ownerType, Class<?> rawType, Type[] actualTypeArguments) {
        this.ownerType = ownerType;
        this.rawType = rawType;
        this.actualTypeArguments = actualTypeArguments;
    }

    @Override
    public Type[] getActualTypeArguments() {
        return actualTypeArguments;
    }

    @Override
    public Type getRawType() {
        return rawType;
    }

    @Override
    public Type getOwnerType() {
        return ownerType;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof ParameterizedType) {
            // Check that information is equivalent
            ParameterizedType that = (ParameterizedType) o;

            if (this == that)
                return true;

            Type thatOwner   = that.getOwnerType();
            Type thatRawType = that.getRawType();

            return
                    Objects.equals(ownerType, thatOwner) &&
                            Objects.equals(rawType, thatRawType) &&
                            Arrays.equals(actualTypeArguments, // avoid clone
                                    that.getActualTypeArguments());
        } else
            return false;
    }

    @Override
    public int hashCode() {
        return
                Arrays.hashCode(actualTypeArguments) ^
                        Objects.hashCode(ownerType) ^
                        Objects.hashCode(rawType);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();

        if (ownerType != null) {
            sb.append(ownerType.getTypeName());

            sb.append("$");

            if (ownerType instanceof ParameterizedTypeImpl) {
                // Find simple name of nested type by removing the
                // shared prefix with owner.
                sb.append(rawType.getName().replace( ((ParameterizedTypeImpl)ownerType).rawType.getName() + "$",
                        ""));
            } else
                sb.append(rawType.getSimpleName());
        } else
            sb.append(rawType.getName());

        if (actualTypeArguments != null) {
            StringJoiner sj = new StringJoiner(", ", "<", ">");
            sj.setEmptyValue("");
            for(Type t: actualTypeArguments) {
                sj.add(t.getTypeName());
            }
            sb.append(sj.toString());
        }

        return sb.toString();
    }
}
