package org.metavm.entity;

import org.metavm.api.Entity;
import org.metavm.api.ValueObject;
import org.metavm.flow.FunctionRef;
import org.metavm.flow.LambdaRef;
import org.metavm.flow.MethodRef;
import org.metavm.flow.ParameterRef;
import org.metavm.object.type.FieldRef;
import org.metavm.object.type.IndexRef;
import org.metavm.wire.*;

@Entity
@Wire(subTypes = {
        @SubType(value = 1, type = LambdaRef.class),
        @SubType(value = 2, type = MethodRef.class),
        @SubType(value = 3, type = ParameterRef.class),
        @SubType(value = 4, type = IndexRef.class),
        @SubType(value = 5, type = FunctionRef.class),
        @SubType(value = 6, type = FieldRef.class)
})
public interface ValueElement extends ValueObject, Element {
}
