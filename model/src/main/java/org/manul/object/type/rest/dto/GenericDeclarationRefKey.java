package org.manul.object.type.rest.dto;

import org.jsonk.Json;
import org.jsonk.SubType;
import org.manul.flow.rest.FunctionRefKey;
import org.manul.flow.rest.MethodRefKey;
import org.manul.util.InstanceInput;
import org.manul.util.MvOutput;
import org.manul.util.WireTypes;

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
