package tech.metavm.transpile;

import org.jetbrains.annotations.Nullable;
import tech.metavm.transpile.ir.IRType;
import tech.metavm.util.NncUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static tech.metavm.transpile.JavaParser.*;

public class SwitchExpressionTypeResolverV2 {

    public static IRType resolve(SwitchExpressionContext switchExpression, ExpressionTypeResolverV2 resolver) {
        return new SwitchExpressionTypeResolverV2(resolver).resolveSwitchExpression(switchExpression);
    }

    private final ExpressionTypeResolverV2 expressionTypeResolver;

    private SwitchExpressionTypeResolverV2(ExpressionTypeResolverV2 expressionTypeResolver) {
        this.expressionTypeResolver = expressionTypeResolver;
    }

    private IRType resolveSwitchExpression(SwitchExpressionContext switchExpression) {
        List<IRType> yieldTypes = extractYieldTypes(switchExpression);
        return IRTypeUtil.getCompatibleType(yieldTypes);
    }

    private List<IRType> extractYieldTypes(SwitchExpressionContext switchExpression) {
        return NncUtils.flatMapAndFilter(
                switchExpression.switchLabeledRule(),
                this::extractYieldType,
                Objects::nonNull
        );
    }

    private List<IRType> extractYieldType(SwitchLabeledRuleContext switchLabeledRule) {
        var outcome = switchLabeledRule.switchRuleOutcome();
        if(outcome.block() != null) {
            return NncUtils.flatMap(
                    outcome.block().blockStatement(),
                    this::extractYieldType
            );
        }
        else {
            return NncUtils.flatMap(
                    outcome.blockStatement(),
                    this::extractYieldType
            );
        }
    }

    private List<IRType> extractYieldType(BlockStatementContext blockStatement) {
        List<IRType> yieldTypes = new ArrayList<>();
        StatementWalker.visit(
                blockStatement,
                new StatementVisitor() {
                    public void visitReturn(@Nullable JavaParser.ExpressionContext expression) {}

                    public void visitYield(ExpressionContext expression) {
                        yieldTypes.add(expressionTypeResolver.resolve(expression));
                    }
                }
        );
        return yieldTypes;
    }


}
