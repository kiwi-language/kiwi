package org.metavm.flow.rest;

import org.metavm.entity.GenericDeclarationRef;
import org.metavm.flow.Function;
import org.metavm.flow.FunctionRef;
import org.metavm.object.instance.core.Id;
import org.metavm.object.type.TypeDefProvider;
import org.metavm.object.type.TypeParser;
import org.metavm.object.type.rest.dto.GenericDeclarationRefKey;
import org.metavm.object.type.rest.dto.TypeKey;
import org.metavm.util.Constants;
import org.metavm.util.InstanceInput;
import org.metavm.util.MvOutput;
import org.metavm.util.Utils;

import java.util.ArrayList;
import java.util.List;

public record FunctionRefKey(
    String rawFlowId,
    List<String> typeArguments
) implements FlowRefKey, GenericDeclarationRefKey {
    @Override
    public int getKind() {
        return 2;
    }

    @Override
    public String toTypeExpression() {
        return "func " + Constants.addIdPrefix(rawFlowId) + (
                typeArguments.isEmpty() ? ""
                        : "<" + Utils.join(typeArguments) + ">"

        );
    }

    public static FunctionRefKey read(InstanceInput input) {
        var rawMethodId = input.readId().toString();
        var typeArgsCount = input.readInt();
        var typeArgs = new ArrayList<String>();
        for (int i = 0; i < typeArgsCount; i++) {
            typeArgs.add(TypeKey.read(input).toTypeExpression());
        }
        return new FunctionRefKey(rawMethodId, typeArgs);
    }

    @Override
    public void write(MvOutput output) {
        output.writeId(Id.parse(rawFlowId));
        output.writeInt(typeArguments.size());
        typeArguments.forEach(t -> TypeKey.fromExpression(t).write(output));
    }

    public FunctionRef toFunctionRef(TypeDefProvider typeDefProvider) {
        return new FunctionRef(
                (Function) typeDefProvider.getTypeDef(Id.parse(rawFlowId)),
                Utils.map(typeArguments, t -> TypeParser.parseType(t, typeDefProvider))
        );
    }

    @Override
    public GenericDeclarationRef toGenericDeclarationRef(TypeDefProvider typeDefProvider) {
        return toFunctionRef(typeDefProvider);
    }

    @Override
    public GenericDeclarationRef resolve(TypeDefProvider typeDefProvider) {
        var rawFunc = (Function) typeDefProvider.getTypeDef(Id.parse(rawFlowId));
        var typeArgs = Utils.map(typeArguments, t -> TypeParser.parseType(t, typeDefProvider));
        return new FunctionRef(rawFunc, typeArgs);
    }
}
