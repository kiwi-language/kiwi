package tech.metavm.object.instance;

import tech.metavm.entity.IEntityContext;
import tech.metavm.flow.Flow;
import tech.metavm.flow.FlowExecutionService;
import tech.metavm.object.instance.persistence.InstancePO;
import tech.metavm.object.instance.rest.FieldValueDTO;
import tech.metavm.object.instance.rest.InstanceParamDTO;
import tech.metavm.object.meta.FunctionType;
import tech.metavm.util.IdentitySet;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;

public class FunctionInstance extends Instance {

    private final Flow flow;
    @Nullable
    private final ClassInstance boundSelf;

    public FunctionInstance(FunctionType type, Flow flow, @Nullable ClassInstance boundSelf) {
        super(type);
        this.flow = flow;
        this.boundSelf = boundSelf;
    }

//    public Instance invoke(List<Instance> arguments, FlowExecutionService executionService) {
//        ClassInstance self = boundSelf != null ? boundSelf : (ClassInstance) arguments.get(0);
//        List<Instance> args = boundSelf != null ? arguments : arguments.subList(1, arguments.size());
//    }

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
    public FieldValueDTO toFieldValueDTO() {
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
