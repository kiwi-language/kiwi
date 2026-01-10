package org.manul.flow.rest;

import org.jsonk.Json;
import org.manul.object.instance.core.Id;
import org.manul.object.type.rest.dto.GenericDeclarationRefKey;
import org.manul.object.type.rest.dto.TypeKey;
import org.manul.util.Constants;
import org.manul.util.InstanceInput;
import org.manul.util.MvOutput;
import org.manul.util.Utils;

import java.util.ArrayList;
import java.util.List;

@Json
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

}
