package tech.metavm.flow;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tech.metavm.entity.EntityContext;
import tech.metavm.entity.EntityContextFactory;
import tech.metavm.flow.rest.FieldValueDTO;
import tech.metavm.flow.rest.FlowExecutionRequest;
import tech.metavm.object.instance.Instance;
import tech.metavm.object.instance.InstanceContext;
import tech.metavm.object.instance.InstanceContextFactory;
import tech.metavm.object.instance.log.InstanceLogService;
import tech.metavm.object.instance.rest.InstanceDTO;
import tech.metavm.object.instance.rest.InstanceFieldDTO;
import tech.metavm.util.NncUtils;

import java.util.List;

@Component
public class FlowExecutionService {

    @Autowired
    private EntityContextFactory entityContextFactory;

    @Autowired
    private InstanceContextFactory instanceContextFactory;

    @Autowired
    private InstanceLogService instanceLogService;

    public InstanceDTO execute(FlowExecutionRequest request) {
        EntityContext context = newContext();
        FlowRT flow = context.get(FlowRT.class, request.flowId());
        InstanceContext instanceContext = newInstanceContext(context);
        Instance self = instanceContext.get(request.instanceId());
        InstanceDTO argument = createArgument(flow.getInputType().getId(), request.fields());
        FlowStack stack = new FlowStack(flow, self, argument, instanceContext);
        InstanceDTO result = stack.execute();
        processLogs(instanceContext);
        return result;
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

    private void processLogs(InstanceContext context) {
        if(context.isAsyncLogProcessing()) {
            instanceLogService.asyncProcess(context.getLogs());
        }
        else {
            instanceLogService.process(context.getLogs());
        }
    }

    private EntityContext newContext() {
        return entityContextFactory.newContext();
    }

    private InstanceContext newInstanceContext(EntityContext entityContext) {
        return instanceContextFactory.createContext(entityContext);
    }

}
