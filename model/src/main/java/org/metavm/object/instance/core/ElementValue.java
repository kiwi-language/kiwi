package org.metavm.object.instance.core;

import org.metavm.entity.IEntityContext;
import org.metavm.entity.ValueElement;
import org.metavm.object.instance.rest.FieldValue;
import org.metavm.object.instance.rest.InstanceParam;
import org.metavm.util.InstanceOutput;

public abstract class ElementValue extends ValueElement implements Value {

    @Override
    public boolean isReference() {
        return false;
    }

    @Override
    public FieldValue toFieldValueDTO() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getTitle() {
        return toString();
    }

    @Override
    public void writeInstance(InstanceOutput output) {
        write(output);
    }

    @Override
    public Object toSearchConditionValue() {
        throw new UnsupportedOperationException();
    }

    @Override
    public InstanceParam getParam() {
        throw new UnsupportedOperationException();
    }

    @Override
    public <R> R accept(ValueVisitor<R> visitor) {
        return visitor.visitElement(this);
    }

    @Override
    public <R> void acceptReferences(ValueVisitor<R> visitor) {

    }

    @Override
    public <R> void acceptChildren(ValueVisitor<R> visitor) {

    }

    @Override
    public boolean isMutable() {
        return false;
    }

    @Override
    public Object toJson(IEntityContext context) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Id getId() {
        return super.getId();
    }

    @Override
    public void writeTree(TreeWriter treeWriter) {
        treeWriter.write(toString());
    }
}
