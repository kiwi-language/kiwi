package tech.metavm.transpile;

public interface SourceType {

    boolean isAssignableFrom(SourceType that);

    Class<?> getReflectClass();

    default String getReflectClassName() {
        return getReflectClass().getName();
    }
}
