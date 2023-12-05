package tech.metavm.object.instance.core;

import tech.metavm.flow.FlowExecResult;
import tech.metavm.object.instance.rest.FieldValue;
import tech.metavm.object.instance.rest.InstanceParam;
import tech.metavm.object.type.FunctionType;
import tech.metavm.util.InstanceInput;
import tech.metavm.util.InstanceOutput;

import java.util.List;
import java.util.Set;

public abstract class FunctionInstance extends Instance {

    public FunctionInstance(FunctionType type) {
        super(type);
    }

    @Override
    public void acceptReferences(InstanceVisitor visitor) {
    }

    @Override
    public void acceptChildren(InstanceVisitor visitor) {
    }

    public abstract FlowExecResult execute(List<Instance> arguments, IInstanceContext context);

    @Override
    public boolean isInitialized() {
        return true;
    }

    @Override
    public FunctionType getType() {
        return (FunctionType) super.getType();
    }

//    public abstract Frame createFrame(FlowStack stack, List<Instance> arguments);

    @Override
    public boolean isReference() {
        return false;
    }

    @Override
    public Set<Instance> getRefInstances() {
        return null;
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
    public void writeTo(InstanceOutput output, boolean includeChildren) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void readFrom(InstanceInput input) {
        throw new UnsupportedOperationException();
    }
}
