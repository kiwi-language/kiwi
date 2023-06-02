package tech.metavm.transpile;

public class VoidSourceType implements SourceType{
    @Override
    public boolean isAssignableFrom(SourceType that) {
        return false;
    }

    @Override
    public Class<?> getReflectClass() {
        return void.class;
    }
}
