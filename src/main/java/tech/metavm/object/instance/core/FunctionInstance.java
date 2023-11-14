package tech.metavm.object.instance.core;

import tech.metavm.flow.FlowStack;
import tech.metavm.flow.Frame;
import tech.metavm.object.instance.core.Instance;
import tech.metavm.object.instance.persistence.InstancePO;
import tech.metavm.object.instance.rest.FieldValue;
import tech.metavm.object.instance.rest.InstanceParamDTO;
import tech.metavm.object.meta.FunctionType;
import tech.metavm.util.IdentitySet;

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

    @Override
    public FunctionType getType() {
        return (FunctionType) super.getType();
    }

    public abstract Frame createFrame(FlowStack stack, List<Instance> arguments);

    @Override
    public Object toColumnValue(long tenantId, IdentitySet<Instance> visited) {
        return null;
    }

    @Override
    public boolean isReference() {
        return false;
    }

    @Override
    public Set<Instance> getRefInstances() {
        return null;
    }

    @Override
    public InstancePO toPO(long tenantId) {
        return null;
    }

    @Override
    InstancePO toPO(long tenantId, IdentitySet<Instance> visited) {
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
    protected InstanceParamDTO getParam() {
        return null;
    }
}
