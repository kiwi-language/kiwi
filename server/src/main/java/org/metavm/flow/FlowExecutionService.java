package org.metavm.flow;

import lombok.extern.slf4j.Slf4j;
import org.metavm.entity.EntityContextFactory;
import org.metavm.entity.EntityContextFactoryAware;
import org.metavm.entity.IEntityContext;
import org.metavm.entity.natives.NativeMethods;
import org.metavm.entity.natives.ThrowableNative;
import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.instance.core.ClassInstanceBuilder;
import org.metavm.object.instance.core.StringValue;
import org.metavm.object.instance.core.Value;
import org.metavm.util.ContextUtil;
import org.metavm.util.FlowExecutionException;
import org.metavm.util.InternalException;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

@Component
@Slf4j
public class FlowExecutionService extends EntityContextFactoryAware {

    public FlowExecutionService(EntityContextFactory entityContextFactory) {
        super(entityContextFactory);
    }

    public Value executeInternal(FlowRef flow, @Nullable ClassInstance self, List<Value> arguments, IEntityContext context) {
        try(var ignored = ContextUtil.getProfiler().enter("FlowExecutionService.executeInternal")) {
            if (flow instanceof MethodRef method && method.isInstanceMethod()) {
                if (method.isConstructor()) {
                    self = ClassInstanceBuilder.newBuilder(((MethodRef) flow).getDeclaringType()).build();
                    context.getInstanceContext().bind(self);
                } else
                    flow = Objects.requireNonNull(self).getType().getOverride(method);
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
    }

}
