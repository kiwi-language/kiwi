package org.metavm.entity.natives;

import org.metavm.flow.FlowExecResult;
import org.metavm.flow.FunctionRef;
import org.metavm.object.instance.core.Value;

import java.util.List;

@FunctionalInterface
public interface FunctionImpl {

    FlowExecResult run(FunctionRef function, List<? extends Value> arguments, CallContext callContext);

}
