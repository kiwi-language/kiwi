package org.metavm.flow.rest;

import org.jsonk.Json;
import org.metavm.object.instance.core.Id;
import org.metavm.object.type.rest.dto.GenericDeclarationRefKey;
import org.metavm.object.type.rest.dto.TypeKey;
import org.metavm.util.Constants;
import org.metavm.util.InstanceInput;
import org.metavm.util.MvOutput;
import org.metavm.util.Utils;

import java.util.ArrayList;
import java.util.List;

@Json
public record MethodRefKey(
        String declaringType,
        String rawFlowId,
        List<String> typeArguments
) implements FlowRefKey, GenericDeclarationRefKey {

    @Override
    public int getKind() {
        return 1;
    }

    @Override
    public String toTypeExpression() {
        return declaringType + "::" + Constants.addIdPrefix(rawFlowId) +
                (
                        typeArguments.isEmpty() ? "" :
                                "<" + Utils.join(typeArguments, String::toString) + ">"
                );
    }

    public static MethodRefKey read(InstanceInput input) {
        var classType = TypeKey.read(input).toTypeExpression();
        var rawMethodId = input.readId().toString();
        var typeArgsCount = input.readInt();
        var typeArgs = new ArrayList<String>();
        for (int i = 0; i < typeArgsCount; i++) {
            typeArgs.add(TypeKey.read(input).toTypeExpression());
        }
        return new MethodRefKey(classType, rawMethodId, typeArgs);
    }

    @Override
    public void write(MvOutput output) {
        TypeKey.fromExpression(declaringType).write(output);
        output.writeId(Id.parse(rawFlowId));
        output.writeInt(typeArguments.size());
        typeArguments.forEach(t -> TypeKey.fromExpression(t).write(output));
    }

}
