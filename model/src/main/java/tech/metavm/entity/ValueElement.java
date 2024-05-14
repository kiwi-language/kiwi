package tech.metavm.entity;

public abstract class ValueElement extends Element implements Value {

    @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
    @Override
    public boolean equals(Object obj) {
        EntityUtils.ensureTreeInitialized(obj);
        return equals0(obj);
    }

    protected abstract boolean equals0(Object obj);

}
