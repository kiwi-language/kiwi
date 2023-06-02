package tech.metavm.transpile.ir;

import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.List;

public record UnresolvedStaticMethodReference(
        IRType declaringType,
        String methodName,
        @Nullable List<IRType> typeArguments
) implements UnresolvedFunctionExpression {

    @Override
    public List<? extends IRFunction> functions() {
        return IRUtil.getRawClass(declaringType).getConstructors();
    }

    @Override
    public IRExpression resolve(IRType type) {
        var rawClass = IRUtil.getRawClass(declaringType);
        var method = rawClass.getMethod(methodName, IRUtil.getFunctionParameterTypes(type));
        return new StaticMethodReference(declaringType, method, typeArguments);
    }

    @Override
    public boolean isTypeMatched(IRType type) {
        var constructors = IRUtil.getRawClass(declaringType).getConstructors();
        if(typeArguments != null) {
            var pConstructors = NncUtils.filterAndMap(
                    constructors,
                    c -> c.isTypeArgumentsMatched(typeArguments),
                    c -> new ConstructorReference(declaringType, c, typeArguments)
            );

        }

        return UnresolvedFunctionExpression.super.isTypeMatched(type);
    }
}
