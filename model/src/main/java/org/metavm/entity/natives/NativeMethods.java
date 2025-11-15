package org.metavm.entity.natives;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.metavm.flow.FlowExecResult;
import org.metavm.flow.Method;
import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.instance.core.Value;
import org.metavm.util.InternalException;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

@Slf4j
public class NativeMethods {

    public static @NotNull FlowExecResult invoke(Method method, @Nullable Value self, List<? extends Value> arguments, CallContext callContext) {
        try {
            if (method.isStatic()) {
                var nativeFunc = Objects.requireNonNull(method.getNativeFunction());
                var result = nativeFunc.apply(null, arguments, callContext);
                if (method.getReturnType().isVoid()) {
                    return new FlowExecResult(null, null);
                } else {
                    return new FlowExecResult(result, null);
                }
            } else {
                if (self instanceof Reference ref && ref.get() instanceof ClassInstance classInstance) {
                    var nativeFuc = Objects.requireNonNull(method.getNativeFunction(), () -> "Failed find native function method: " + method.getQualifiedName());
                    var result = nativeFuc.apply(classInstance, arguments, callContext);
                    if (method.getReturnType().isVoid()) {
                        return new FlowExecResult(null, null);
                    } else {
                        return new FlowExecResult(result, null);
                    }
                } else
                    throw new InternalException("Native invocation is not supported for non class instance: " + self.resolveObject());
            }
        }
        catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

}
