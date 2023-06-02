package tech.metavm.transpile.ir;

import tech.metavm.transpile.ir.gen2.Graph;

import javax.annotation.Nullable;
import java.util.List;

public record PartiallyResolvedCreator(
        IRClass klass,
        IRConstructor constructor,
        @Nullable IRExpression owner,
        List<IRExpression> arguments,
        Graph graph
) implements IRExpression{

    @Override
    public IRType type() {
        return null;
    }

    public CreatorExpression toCreator(IRType instanceType, List<IRType> typeArguments, List<IRExpression> resolvedArguments) {
        return new CreatorExpression(
                instanceType,
                constructor,
                typeArguments,
                resolvedArguments,
                owner
        );
    }

}
