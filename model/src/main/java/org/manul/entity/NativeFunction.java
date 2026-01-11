package org.manul.entity;

import org.manul.entity.natives.CallContext;
import org.manul.object.instance.core.ClassInstance;
import org.manul.object.instance.core.Value;

import java.util.List;

@FunctionalInterface
public interface NativeFunction {

    Value apply(ClassInstance self, List<? extends Value> args, CallContext callContext);

}
