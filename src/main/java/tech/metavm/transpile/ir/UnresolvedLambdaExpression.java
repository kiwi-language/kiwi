package tech.metavm.transpile.ir;

import tech.metavm.transpile.IRBuilder;
import tech.metavm.transpile.IRTypeUtil;

import java.util.List;

import static tech.metavm.transpile.JavaParser.LambdaExpressionContext;

public record UnresolvedLambdaExpression(
        List<IRType> parameterTypes,
        boolean hasReturn,
        LambdaExpressionContext context,
        IRBuilder irBuilder
) implements UnresolvedExpression {

    @Override
    public boolean isTypeMatched(IRType type) {
        return IRUtil.isFunctionTypeAssignable(
                new FunctionType(
                        parameterTypes,
                        hasReturn ? IRWildCardType.asterisk() : IRTypeUtil.voidType()
                ),
                type
        );
    }

    @Override
    public IRExpression resolve(IRType type) {
        return irBuilder.parseLambdaExpression(context, type);
    }
}
