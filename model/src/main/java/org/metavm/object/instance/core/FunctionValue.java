package org.metavm.object.instance.core;

import org.metavm.entity.IEntityContext;
import org.metavm.entity.natives.CallContext;
import org.metavm.flow.FlowExecResult;
import org.metavm.object.instance.rest.FieldValue;
import org.metavm.object.instance.rest.InstanceParam;
import org.metavm.object.type.FunctionType;

import javax.annotation.Nullable;
import java.util.List;

public abstract class FunctionValue extends Value {

    public FunctionValue() {
    }

    @Override
    public <R> void acceptReferences(ValueVisitor<R> visitor) {
    }

    @Override
    public <R> void acceptChildren(ValueVisitor<R> visitor) {
    }

    @Override
    public @Nullable Id tryGetId() {
        return null;
    }

    public abstract FlowExecResult execute(List<? extends Value> arguments, CallContext callContext);

    @Override
    public abstract FunctionType getType();

//    public abstract Frame createFrame(FlowStack stack, List<Instance> arguments);

    @Override
    public boolean isReference() {
        return false;
    }

    @Override
    public FieldValue toFieldValueDTO() {
        return null;
    }

    @Override
    public String getTitle() {
        return null;
    }

    @Override
    protected InstanceParam getParam() {
        return null;
    }

    @Override
    public Object toSearchConditionValue() {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void writeTree(TreeWriter treeWriter) {
        treeWriter.writeLine("function");
    }

    @Override
    public Object toJson(IEntityContext context) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isMutable() {
        return false;
    }
}
