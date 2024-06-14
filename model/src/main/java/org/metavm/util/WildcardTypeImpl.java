package org.metavm.util;

import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.Arrays;

@SuppressWarnings("ClassCanBeRecord")
public class WildcardTypeImpl implements WildcardType {

    public static WildcardType createAsterisk() {
        return new WildcardTypeImpl(new Type[0], new Type[0]);
    }

    public static WildcardType createExtends(Type upperBound) {
        return new WildcardTypeImpl(
                new Type[] {upperBound},
                new Type[0]
        );
    }

    public static WildcardType createSuper(Type lowerBound) {
        return new WildcardTypeImpl(
                new Type[0],
                new Type[] {lowerBound}
        );
    }

    private final Type[] upperBounds;
    private final Type[] lowerBounds;

    public WildcardTypeImpl(Type[] upperBounds, Type[] lowerBounds) {
        this.upperBounds = upperBounds;
        this.lowerBounds = lowerBounds;
    }

    @Override
    public Type[] getUpperBounds() {
        return this.upperBounds.clone();
    }

    @Override
    public Type[] getLowerBounds() {
        return this.lowerBounds.clone();
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof WildcardType type) {
            return Arrays.equals(this.upperBounds, type.getUpperBounds())
                    && Arrays.equals(this.lowerBounds, type.getLowerBounds());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(this.upperBounds)
                ^ Arrays.hashCode(this.lowerBounds);
    }

    @Override
    public String toString() {
        StringBuilder sb;
        Type[] bounds;
        if (this.lowerBounds.length == 0) {
            if (this.upperBounds.length == 0 || Object.class == this.upperBounds[0]) {
                return "?";
            }
            bounds = this.upperBounds;
            sb = new StringBuilder("? extends ");
        }
        else {
            bounds = this.lowerBounds;
            sb = new StringBuilder("? super ");
        }
        for (int i = 0; i < bounds.length; i++) {
            if (i > 0) {
                sb.append(" & ");
            }
            sb.append((bounds[i] instanceof Class)
                    ? ((Class) bounds[i]).getName()
                    : bounds[i].toString());
        }
        return sb.toString();
    }
}
