package tech.metavm.flow;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tech.metavm.entity.IEntityContext;
import tech.metavm.entity.InstanceContext;
import tech.metavm.entity.InstanceContextFactory;
import tech.metavm.flow.rest.FlowExecutionRequest;
import tech.metavm.object.instance.Instance;
import tech.metavm.object.instance.rest.InstanceDTO;
import tech.metavm.object.instance.rest.InstanceFieldDTO;
import tech.metavm.util.NncUtils;

import java.util.List;

@Component
public class FlowExecutionService {

    private final InstanceContextFactory instanceContextFactory;

    public FlowExecutionService(InstanceContextFactory instanceContextFactory) {
        this.instanceContextFactory = instanceContextFactory;
    }

    @Transactional
    public InstanceDTO execute(FlowExecutionRequest request) {
        InstanceContext context = newContext();
        IEntityContext entityContext = context.getEntityContext();
        FlowRT flow = entityContext.getEntity(FlowRT.class, request.flowId());
        Instance self = context.get(request.instanceId());
        InstanceDTO argument = createArgument(flow.getInputType().getId(), request.fields());
        FlowStack stack = new FlowStack(flow, self, argument, context);
        Instance result = stack.execute();
        context.finish();
        return NncUtils.get(result, Instance::toDTO);
    }

    private InstanceDTO createArgument(long typeId, List<InstanceFieldDTO> fields) {
        return InstanceDTO.valueOf(
                typeId,
                NncUtils.map(fields, this::createField)
        );
    }

    private InstanceFieldDTO createField(InstanceFieldDTO fieldValueDTO) {
//        return InstanceFieldDTO.valueOf(
//                fieldValueDTO.fieldId(),
//                fieldValueDTO
//        );
        return fieldValueDTO;
    }

    private InstanceContext newContext() {
        return instanceContextFactory.newContext();
    }

}
