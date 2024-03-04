package tech.metavm.flow;

import tech.metavm.object.instance.core.BooleanInstance;
import tech.metavm.object.instance.core.ClassInstance;
import tech.metavm.object.instance.core.Instance;

import javax.annotation.Nullable;

public record FlowExecResult(
        @Nullable Instance ret,
        @Nullable ClassInstance exception
) {
    public boolean booleanRet() {
        if(ret instanceof BooleanInstance booleanInstance)
            return booleanInstance.getValue();
        else
            throw new IllegalStateException("Not a boolean instance");
    }
}
