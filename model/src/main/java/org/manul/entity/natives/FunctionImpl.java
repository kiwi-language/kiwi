package org.manul.entity.natives;

import org.manul.flow.FlowExecResult;
import org.manul.flow.FunctionRef;
import org.manul.object.instance.core.Value;

import java.util.List;

@FunctionalInterface
public interface FunctionImpl {

    FlowExecResult run(FunctionRef function, List<? extends Value> arguments, CallContext callContext);

}
