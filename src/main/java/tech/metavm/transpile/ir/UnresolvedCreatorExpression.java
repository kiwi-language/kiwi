package tech.metavm.transpile.ir;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Function;

public record UnresolvedCreatorExpression(
        IRClass klass,
        IRConstructor constructor,
        List<IRExpression> arguments,
        List<IRType> methodTypeArguments,
        @Nullable IRExpression owner, Function<IRType, IRType> onResolved
) implements UnresolvedExpression {

    @Override
    public boolean isTypeMatched(IRType type) {
        return IRUtil.getRawClass(type).equals(klass);
    }

    @Override
    public IRExpression resolve(IRType type) {
        return new CreatorExpression(onResolved.apply(type), constructor, methodTypeArguments, arguments, owner);
    }

    @Override
    public IRType onResolved(IRType type) {
        return onResolved.apply(type);
    }
}
