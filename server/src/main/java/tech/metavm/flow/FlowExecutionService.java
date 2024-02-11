package tech.metavm.flow;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tech.metavm.entity.EntityContextFactory;
import tech.metavm.entity.EntityContextFactoryBean;
import tech.metavm.entity.IEntityContext;
import tech.metavm.entity.natives.NativeMethods;
import tech.metavm.entity.natives.ThrowableNative;
import tech.metavm.flow.rest.FlowExecutionRequest;
import tech.metavm.object.instance.InstanceFactory;
import tech.metavm.object.instance.core.*;
import tech.metavm.object.instance.rest.InstanceDTO;
import tech.metavm.util.FlowExecutionException;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
public class FlowExecutionService extends EntityContextFactoryBean  {

    public FlowExecutionService(EntityContextFactory entityContextFactory) {
        super(entityContextFactory);
    }

    @Transactional
    public InstanceDTO execute(FlowExecutionRequest request) {
        try (var context = newContext()) {
            var flow = context.getEntity(Flow.class, request.flowId());
            var self = NncUtils.get(request.instanceId(),
                    id -> (ClassInstance) context.getInstanceContext().get(Id.parse(id)));
            var arguments = new ArrayList<Instance>();
            NncUtils.biForEach(
                    request.arguments(),
                    flow.getParameterTypes(),
                    (arg, paramType) -> arguments.add(
                            InstanceFactory.resolveValue(arg, paramType, context)
                    )
            );
            var result = executeInternal(flow, self, arguments, context);
            context.finish();
            return NncUtils.get(result, Instance::toDTO);
        }
    }

    public Instance executeInternal(Flow flow, @Nullable ClassInstance self, List<Instance> arguments, IEntityContext context) {
        if (flow instanceof Method method && method.isInstanceMethod()) {
            if(method.isConstructor()) {
                self = ClassInstanceBuilder.newBuilder(((Method) flow).getDeclaringType()).build();
                context.getInstanceContext().bind(self);
            }
            else
                flow = Objects.requireNonNull(self).getType().resolveMethod(method, context.getGenericContext());
        }
        var result = flow.execute(self, arguments, context.getInstanceContext(), context.getGenericContext());
        if (result.exception() == null)
            return result.ret();
        else {
            ThrowableNative nativeObject = (ThrowableNative) NativeMethods.getNativeObject(result.exception());
            String message = nativeObject.getMessage() instanceof StringInstance str ? str.getValue() : "执行失败";
            throw new FlowExecutionException(message);
        }
    }

}
