package tech.metavm.object.instance.core;

import tech.metavm.flow.FlowExecResult;
import tech.metavm.flow.ParameterizedFlowProvider;
import tech.metavm.object.instance.rest.FieldValue;
import tech.metavm.object.instance.rest.InstanceParam;
import tech.metavm.object.type.FunctionType;
import tech.metavm.util.InstanceOutput;

import javax.annotation.Nullable;
import java.util.List;

public abstract class FunctionInstance extends Instance {

    public FunctionInstance(FunctionType type) {
        super(type);
    }

    @Override
    public <R> void acceptReferences(InstanceVisitor<R> visitor) {
    }

    @Override
    public <R> void acceptChildren(InstanceVisitor<R> visitor) {
    }

    @Override
    public @Nullable Id getInstanceId() {
        return null;
    }

    public abstract FlowExecResult execute(List<Instance> arguments, InstanceRepository instanceRepository, ParameterizedFlowProvider parameterizedFlowProvider);

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
    public Object toSearchConditionValue() {
        throw new UnsupportedOperationException();
    }
}
