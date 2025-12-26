package org.metavm.object.type.rest.dto;

import org.jsonk.Json;
import org.jsonk.SubType;
import org.metavm.flow.rest.FunctionRefKey;
import org.metavm.flow.rest.MethodRefKey;
import org.metavm.util.InstanceInput;
import org.metavm.util.MvOutput;
import org.metavm.util.WireTypes;

@Json(
        typeProperty = "kind",
        subTypes = {
                @SubType(value = "class", type = ClassTypeKey.class),
                @SubType(value = "method", type = MethodRefKey.class),
                @SubType(value = "function", type = FunctionRefKey.class)
        }
)
public interface GenericDeclarationRefKey {

    String toTypeExpression();

    static GenericDeclarationRefKey read(InstanceInput input) {
        int code = input.read();
        if(code == WireTypes.METHOD_REF)
            return MethodRefKey.read(input);
        else if(code == WireTypes.FUNCTION_REF)
            return FunctionRefKey.read(input);
        else
            return (GenericDeclarationRefKey) TypeKey.read(code, input);
    }

    void write(MvOutput output);

}
