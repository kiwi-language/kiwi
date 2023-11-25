package tech.metavm.flow;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tech.metavm.entity.IEntityContext;
import tech.metavm.entity.InstanceContextFactory;
import tech.metavm.entity.natives.NativeInvoker;
import tech.metavm.entity.natives.ThrowableNative;
import tech.metavm.flow.rest.FlowExecutionRequest;
import tech.metavm.object.instance.InstanceFactory;
import tech.metavm.object.instance.core.ClassInstance;
import tech.metavm.object.instance.core.IInstanceContext;
import tech.metavm.object.instance.core.Instance;
import tech.metavm.object.instance.core.StringInstance;
import tech.metavm.object.instance.rest.InstanceDTO;
import tech.metavm.util.ContextUtil;
import tech.metavm.util.FlowExecutionException;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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

    public Instance executeInternal(Flow flow, @Nullable ClassInstance self, List<Instance> arguments, IInstanceContext context) {
        if (!flow.isStatic())
            flow = Objects.requireNonNull(self).getType().resolveFlow(flow, context.getEntityContext());
        var result = flow.execute(self, arguments, context);
        if (result.exception() == null)
            return result.ret();
        else {
            ThrowableNative nativeObject = (ThrowableNative) NativeInvoker.getNativeObject(result.exception());
            String message = nativeObject.getMessage() instanceof StringInstance str ? str.getValue() : "执行失败";
            throw new FlowExecutionException(message);
        }
    }

    private IInstanceContext newContext() {
        return instanceContextFactory.newContext(ContextUtil.getTenantId(), true);
    }

}
