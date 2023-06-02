package tech.metavm.transpile;

public class NullSourceType implements SourceType {
    @Override
    public boolean isAssignableFrom(SourceType that) {
        return false;
    }

    @Override
    public Class<?> getReflectClass() {
        return null;
    }
}
