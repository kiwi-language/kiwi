package org.metavm.object.type;

public class EmptySubstitutor implements ISubstitutor {
    @Override
    public Type substitute(Type type) {
        return type;
    }
}
