package org.metavm.flow;

import org.metavm.object.instance.core.BooleanValue;
import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.instance.core.Value;

import javax.annotation.Nullable;

public record FlowExecResult(
        @Nullable Value ret,
        @Nullable ClassInstance exception
) {

    public static FlowExecResult of(Value ret) {
        return new FlowExecResult(ret, null);
    }

    public static FlowExecResult ofException(ClassInstance exception) {
        return new FlowExecResult(null, exception);
    }

    public boolean booleanRet() {
        if(ret instanceof BooleanValue booleanInstance)
            return booleanInstance.getValue();
        else
            throw new IllegalStateException("Not a boolean instance");
    }
}
