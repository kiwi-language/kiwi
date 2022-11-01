package tech.metavm.flow;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tech.metavm.entity.EntityContext;
import tech.metavm.entity.EntityContextFactory;
import tech.metavm.flow.rest.FieldValueDTO;
import tech.metavm.flow.rest.FlowExecutionRequest;
import tech.metavm.object.instance.Instance;
import tech.metavm.object.instance.InstanceContext;
import tech.metavm.object.instance.rest.InstanceDTO;
import tech.metavm.object.instance.rest.InstanceFieldDTO;
import tech.metavm.util.NncUtils;

import java.util.List;

@Component
public class FlowExecutionService {

    @Autowired
    private EntityContextFactory entityContextFactory;

    @Transactional
    public InstanceDTO execute(FlowExecutionRequest request) {
        EntityContext context = newContext();
        FlowRT flow = context.get(FlowRT.class, request.flowId());
        InstanceContext instanceContext = context.getInstanceContext();
        Instance self = instanceContext.get(request.instanceId());
        InstanceDTO argument = createArgument(flow.getInputType().getId(), request.fields());
        FlowStack stack = new FlowStack(flow, self, argument, instanceContext);
        Instance result = stack.execute();
        instanceContext.finish();
        return NncUtils.get(result, Instance::toDTO);
    }

    private InstanceDTO createArgument(long typeId, List<FieldValueDTO> fields) {
        return InstanceDTO.valueOf(
                typeId,
                NncUtils.map(fields, this::createField)
        );
    }

    private InstanceFieldDTO createField(FieldValueDTO fieldValueDTO) {
        return InstanceFieldDTO.valueOf(
                fieldValueDTO.fieldId(),
                fieldValueDTO.value()
        );
    }

    private EntityContext newContext() {
        return entityContextFactory.newContext();
    }

}
