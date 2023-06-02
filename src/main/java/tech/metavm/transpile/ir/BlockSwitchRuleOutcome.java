package tech.metavm.transpile.ir;

import tech.metavm.transpile.BlockTypeResolver;

import java.util.List;

public record BlockSwitchRuleOutcome(
        CodeBlock block
) implements SwitchRuleOutcome {
    @Override
    public IRType getType() {
        return BlockTypeResolver.resolve(
                Yield.class,
                Yield::expression,
                block.getStatements()
        );
    }

    @Override
    public List<Statement> getStatements() {
        return List.of(block);
    }
}
