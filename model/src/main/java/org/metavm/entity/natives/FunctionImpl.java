package org.metavm.entity.natives;

import org.metavm.flow.FlowExecResult;
import org.metavm.flow.Function;
import org.metavm.object.instance.core.Instance;

import java.util.List;

@FunctionalInterface
public interface FunctionImpl {

    FlowExecResult run(Function function, List<? extends Instance> arguments, CallContext callContext);

}
