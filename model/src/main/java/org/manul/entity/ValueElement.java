package org.manul.entity;

import org.manul.api.Entity;
import org.manul.api.ValueObject;
import org.manul.flow.FunctionRef;
import org.manul.flow.LambdaRef;
import org.manul.flow.MethodRef;
import org.manul.flow.ParameterRef;
import org.manul.object.type.FieldRef;
import org.manul.object.type.IndexRef;
import org.manul.wire.*;

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
