package org.metavm.entity;

import org.metavm.entity.natives.CallContext;
import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.instance.core.Value;

import java.util.List;

@FunctionalInterface
public interface NativeFunction {

    Value apply(ClassInstance self, List<? extends Value> args, CallContext callContext);

}
