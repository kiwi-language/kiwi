package org.metavm.object.instance.core;

import org.metavm.flow.ClosureContext;
import org.metavm.object.type.Type;
import org.metavm.util.MvOutput;

public class ValueReference implements Reference {

    private final Instance instance;

    public ValueReference(Instance instance) {
        assert instance.isValue();
        this.instance = instance;
    }

    @Override
    public Instance get() {
        return instance;
    }

    @Override
    public Type getValueType() {
        return instance.getInstanceType();
    }

    @Override
    public String getTitle() {
        return instance.getTitle();
    }

    @Override
    public void writeInstance(MvOutput output) {
        resolveDurable().write(output);
    }

    @Override
    public void write(MvOutput output) {
        writeInstance(output);
    }

    @Override
    public Object toSearchConditionValue() {
        return null;
    }

    @Override
    public <R> R accept(ValueVisitor<R> visitor) {
        return null;
    }

    @Override
    public void writeTree(TreeWriter treeWriter) {

    }

    @Override
    public Object toJson() {
        return null;
    }

    @Override
    public boolean isRemoved() {
        return false;
    }

    @Override
    public boolean isArray() {
        return instance instanceof ArrayInstance;
    }

    @Override
    public boolean isObject() {
        return instance instanceof ClassInstance;
    }

    @Override
    public ClosureContext getClosureContext() {
        var r = get();
        return r instanceof ClassInstance obj ? obj.getClosureContext() : null;
    }

    @Override
    public boolean isResolved() {
        return true;
    }

    @Override
    public int hashCode() {
        return instance.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof ValueReference that && instance == that.instance;
    }

}
