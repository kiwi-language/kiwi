//package tech.metavm.transpile;
//
//import org.jetbrains.annotations.Nullable;
//import tech.metavm.util.NncUtils;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Objects;
//
//import static tech.metavm.transpile.JavaParser.*;
//
//public class SwitchExpressionTypeResolver {
//
//    public static SourceType resolve(SwitchExpressionContext switchExpression, ExpressionTypeResolver resolver) {
//        return new SwitchExpressionTypeResolver(resolver).resolveSwitchExpression(switchExpression);
//    }
//
//    private final ExpressionTypeResolver expressionTypeResolver;
//
//    private SwitchExpressionTypeResolver(ExpressionTypeResolver expressionTypeResolver) {
//        this.expressionTypeResolver = expressionTypeResolver;
//    }
//
//    private SourceType resolveSwitchExpression(SwitchExpressionContext switchExpression) {
//        List<SourceType> yieldTypes = extractYieldTypes(switchExpression);
//        return SourceTypeUtil.getCompatibleType(yieldTypes);
//    }
//
//    private List<SourceType> extractYieldTypes(SwitchExpressionContext switchExpression) {
//        return NncUtils.flatMapAndFilter(
//                switchExpression.switchLabeledRule(),
//                this::extractYieldType,
//                Objects::nonNull
//        );
//    }
//
//    private List<SourceType> extractYieldType(SwitchLabeledRuleContext switchLabeledRule) {
//        var outcome = switchLabeledRule.switchRuleOutcome();
//        if(outcome.block() != null) {
//            return NncUtils.flatMap(
//                    outcome.block().blockStatement(),
//                    this::extractYieldType
//            );
//        }
//        else {
//            return NncUtils.flatMap(
//                    outcome.blockStatement(),
//                    this::extractYieldType
//            );
//        }
//    }
//
//    private List<SourceType> extractYieldType(BlockStatementContext blockStatement) {
//        List<SourceType> yieldTypes = new ArrayList<>();
//        StatementWalker.visit(
//                blockStatement,
//                new StatementVisitor() {
//                    public void visitReturn(@Nullable JavaParser.ExpressionContext expression) {}
//
//                    public void visitYield(ExpressionContext expression) {
//                        yieldTypes.add(expressionTypeResolver.resolve(expression));
//                    }
//                }
//        );
//        return yieldTypes;
//    }
//
//
//}
