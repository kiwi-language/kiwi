package org.metavm.object.type.rest.dto;

import org.metavm.entity.GenericDeclarationRef;
import org.metavm.flow.rest.FunctionRefDTO;
import org.metavm.flow.rest.MethodRefDTO;
import org.metavm.object.type.TypeDefProvider;
import org.metavm.object.type.antlr.TypeParser;
import org.metavm.util.InstanceInput;

public interface GenericDeclarationRefKey {

    String toTypeExpression();

    GenericDeclarationRef resolve(TypeDefProvider typeDefProvider);

    static GenericDeclarationRefKey fromContext(TypeParser.GenericDeclarationRefContext ctx) {
        if(ctx.classType() != null)
            return (GenericDeclarationRefKey) TypeKey.fromClassTypeContext(ctx.classType());
        else if(ctx.methodRef() != null)
            return MethodRefDTO.fromContext(ctx.methodRef());
        else
            throw new IllegalArgumentException("Can not parse generic declaration ref key from " + ctx.getText());
    }

    static GenericDeclarationRefKey read(InstanceInput input) {
        int code = input.read();
        if(code == TypeKeyCodes.METHOD_REF)
            return MethodRefDTO.read(input);
        else if(code == TypeKeyCodes.FUNCTION_REF)
            return FunctionRefDTO.read(input);
        else
            return (GenericDeclarationRefKey) TypeKey.read(code, input);
    }

}
