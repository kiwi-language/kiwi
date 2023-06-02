package tech.metavm.transpile.ir;

import tech.metavm.transpile.ir.gen2.Graph;

import javax.annotation.Nullable;
import java.util.List;

public record UnresolvedCreator(
        IRClass klass,
        @Nullable IRExpression owner,
        List<IRExpression> arguments
) implements IRExpression {

    @Override
    public IRType type() {
        return klass.hasTypeParameters() ? klass.templatePType() : klass;
    }

    public PartiallyResolvedCreator toPartiallyResolved(
            IRConstructor constructor, @Nullable IRExpression resolvedOwner, List<IRExpression> resolvedArgs, Graph graph
    ) {
        return new PartiallyResolvedCreator(klass, constructor, resolvedOwner, resolvedArgs, graph);
    }

}
