package org.metavm.flow.rest;

import org.antlr.v4.runtime.RuleContext;
import org.metavm.entity.GenericDeclarationRef;
import org.metavm.flow.Method;
import org.metavm.flow.MethodRef;
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

    @Override
    public GenericDeclarationRef resolve(TypeDefProvider typeDefProvider) {
        var classType = TypeParser.parseClassType(declaringType, typeDefProvider);
        var rawMethod = (Method) typeDefProvider.getTypeDef(Id.parse(rawFlowId));
        var typeArgs = Utils.map(typeArguments, t -> TypeParser.parseType(t, typeDefProvider));
        return new MethodRef(classType, rawMethod, typeArgs);
    }

    public static MethodRefKey fromContext(org.metavm.object.type.antlr.TypeParser.MethodRefContext ctx) {
        return new MethodRefKey(
                ctx.classType().getText(),
                Constants.removeIdPrefix(ctx.IDENTIFIER().getText()),
                ctx.typeArguments() != null ?
                        Utils.map(ctx.typeArguments().typeList().type(), RuleContext::getText) : List.of()
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

    public MethodRef toMethodRef(TypeDefProvider typeDefProvider) {
        return new MethodRef(
                TypeParser.parseClassType(declaringType, typeDefProvider),
                (Method) typeDefProvider.getTypeDef(Id.parse(rawFlowId)),
                Utils.map(typeArguments, t -> TypeParser.parseType(t, typeDefProvider))
        );
    }

    @Override
    public GenericDeclarationRef toGenericDeclarationRef(TypeDefProvider typeDefProvider) {
        return toMethodRef(typeDefProvider);
    }

}
