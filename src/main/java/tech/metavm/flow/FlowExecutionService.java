package tech.metavm.flow;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tech.metavm.entity.*;
import tech.metavm.entity.natives.NativeInvoker;
import tech.metavm.expression.Expression;
import tech.metavm.expression.StaticFieldExpression;
import tech.metavm.expression.VoidStructuralVisitor;
import tech.metavm.flow.rest.FlowExecutionRequest;
import tech.metavm.object.instance.core.ClassInstance;
import tech.metavm.object.instance.core.IInstanceContext;
import tech.metavm.object.instance.core.Instance;
import tech.metavm.object.instance.InstanceFactory;
import tech.metavm.object.instance.rest.InstanceDTO;
import tech.metavm.object.instance.rest.InstanceFieldDTO;
import tech.metavm.object.type.ClassType;
import tech.metavm.object.type.CompositeType;
import tech.metavm.object.type.Type;
import tech.metavm.util.ContextUtil;
import tech.metavm.util.NncUtils;

import java.util.ArrayList;
import java.util.List;

@Component
public class FlowExecutionService {

    private final InstanceContextFactory instanceContextFactory;

    public FlowExecutionService(InstanceContextFactory instanceContextFactory) {
        this.instanceContextFactory = instanceContextFactory;
    }

    @Transactional
    public InstanceDTO execute(FlowExecutionRequest request) {
        try (IInstanceContext context = newContext()) {
            IEntityContext entityContext = context.getEntityContext();
            Flow flow = entityContext.getEntity(Flow.class, request.flowId());
            Flows.enableCache(flow, entityContext);
            ClassInstance self = (ClassInstance) context.get(request.instanceId());
            List<Instance> arguments = new ArrayList<>();
            NncUtils.biForEach(
                    request.arguments(),
                    flow.getParameterTypes(),
                    (arg, paramType) -> arguments.add(
                            InstanceFactory.resolveValue(arg, paramType, entityContext)
                    )
            );
            Instance result = executeInternal(flow, self, arguments, context);
            context.finish();
            return NncUtils.get(result, Instance::toDTO);
        }
    }

    public Instance executeInternal(Flow flow, ClassInstance self, List<Instance> arguments, IInstanceContext context) {
        if (flow.isAbstract()) {
            flow = self.getType().getOverrideFlowRequired(flow);
        }
        if (flow.isNative()) {
            return NativeInvoker.invoke(flow, self, arguments);
        } else {
            FlowStack stack = new FlowStack(flow, self, arguments, context);
            return stack.execute();
        }
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

    private IInstanceContext newContext() {
        return instanceContextFactory.newContext(ContextUtil.getTenantId(), true);
    }

}
