package org.metavm.object.type.rest.dto;

import org.metavm.entity.GenericDeclarationRef;
import org.metavm.flow.rest.FunctionRefKey;
import org.metavm.flow.rest.MethodRefKey;
import org.metavm.object.type.TypeDefProvider;
import org.metavm.object.type.antlr.TypeParser;
import org.metavm.util.InstanceInput;
import org.metavm.util.MvOutput;
import org.metavm.util.WireTypes;

public interface GenericDeclarationRefKey {

    String toTypeExpression();

    GenericDeclarationRef resolve(TypeDefProvider typeDefProvider);

    static GenericDeclarationRefKey fromContext(TypeParser.GenericDeclarationRefContext ctx) {
        if(ctx.classType() != null)
            return (GenericDeclarationRefKey) TypeKey.fromClassTypeContext(ctx.classType());
        else if(ctx.methodRef() != null)
            return MethodRefKey.fromContext(ctx.methodRef());
        else
            throw new IllegalArgumentException("Can not parse generic declaration ref key from " + ctx.getText());
    }

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

    GenericDeclarationRef toGenericDeclarationRef(TypeDefProvider typeDefProvider);
}
