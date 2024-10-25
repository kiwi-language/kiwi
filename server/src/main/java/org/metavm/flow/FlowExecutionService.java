package org.metavm.flow;

import lombok.extern.slf4j.Slf4j;
import org.metavm.entity.EntityContextFactory;
import org.metavm.entity.EntityContextFactoryAware;
import org.metavm.entity.IEntityContext;
import org.metavm.entity.natives.NativeMethods;
import org.metavm.entity.natives.ThrowableNative;
import org.metavm.flow.rest.FlowExecutionRequest;
import org.metavm.object.instance.InstanceFactory;
import org.metavm.object.instance.core.Value;
import org.metavm.object.instance.core.*;
import org.metavm.object.instance.rest.InstanceDTO;
import org.metavm.util.ContextUtil;
import org.metavm.util.FlowExecutionException;
import org.metavm.util.InternalException;
import org.metavm.util.NncUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
@Slf4j
public class FlowExecutionService extends EntityContextFactoryAware {

    public FlowExecutionService(EntityContextFactory entityContextFactory) {
        super(entityContextFactory);
    }

    @Transactional
    public InstanceDTO execute(FlowExecutionRequest request) {
        try (var context = newContext(); var ignored =ContextUtil.getProfiler().enter("FlowExecutionService.execute")) {
            ContextUtil.setEntityContext(context);
            var flowRef = FlowRef.create(request.flowRef(), context);
            var flow = flowRef.resolve();
            var self = NncUtils.get(request.instanceId(),
                    id -> (ClassInstance) context.getInstanceContext().get(Id.parse(id)));
            var arguments = new ArrayList<Value>();
            try(var ignored2 = ContextUtil.getProfiler().enter("FlowExecutionService.execute.resolveArguments")) {
                NncUtils.biForEach(
                        request.arguments(),
                        flow.getParameterTypes(),
                        (arg, paramType) -> arguments.add(
                                InstanceFactory.resolveValue(arg, paramType, context)
                        )
                );
            }
            var result = executeInternal(flow, self, arguments, context);
            context.finish();
            return NncUtils.get(result, Value::toDTO);
        }
        finally {
            ContextUtil.setEntityContext(null);
        }
    }

    public Value executeInternal(Flow flow, @Nullable ClassInstance self, List<Value> arguments, IEntityContext context) {
        try(var ignored = ContextUtil.getProfiler().enter("FlowExecutionService.executeInternal")) {
            ContextUtil.setEntityContext(context);
            if (flow instanceof Method method && method.isInstanceMethod()) {
                if (method.isConstructor()) {
                    self = ClassInstanceBuilder.newBuilder(((Method) flow).getDeclaringType().getType()).build();
                    context.getInstanceContext().bind(self);
                } else
                    flow = Objects.requireNonNull(self).getKlass().resolveMethod(method);
            }
            var result = flow.execute(self, arguments, context.getInstanceContext());
            if (result.exception() == null)
                return result.ret();
            else {
                ThrowableNative nativeObject = (ThrowableNative) NativeMethods.getNativeObject(result.exception());
                String message = nativeObject.getMessage() instanceof StringValue str ? str.getValue() : " execution failed";
                throw new FlowExecutionException(message);
            }
        }
        catch(FlowExecutionException e) {
            throw e;
        }
        catch (Exception e) {
           throw new InternalException("Fail to invoke flow: " + flow.getQualifiedName(), e);
        }
        finally {
            ContextUtil.setEntityContext(null);
        }
    }

}
