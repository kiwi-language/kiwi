package tech.metavm.transpile;

import tech.metavm.transpile.JavaParser.*;
import tech.metavm.util.NncUtils;

public class StatementWalker {

    public static void visit(BlockStatementContext blockStatement, StatementVisitor visitor) {
        new StatementWalker(visitor).visitBlockStatement(blockStatement);
    }

    private final StatementVisitor visitor;


    private StatementWalker(StatementVisitor visitor) {
        this.visitor = visitor;
    }

    private void visitStatement(StatementContext statement) {
        if(statement.blockLabel != null) {
            visitBlock(statement.blockLabel);
        }
        else if(statement.RETURN() != null) {
            visitor.visitReturn(statement.statement().size() > 0 ? statement.expression(0) : null);
        }
        else if(statement.YIELD() != null) {
            visitor.visitYield(statement.expression(0));
        }
        else if(statement.IF() != null || statement.FOR() != null || statement.WHILE() != null
                || statement.DO() != null) {
            NncUtils.forEach(statement.statement(), this::visitStatement);
        }
        else if(statement.TRY() != null) {
            visitBlock(statement.block());
            NncUtils.forEach(statement.catchClause(), this::visitCatchClause);
            visitBlock(statement.finallyBlock().block());
        }
        else if(statement.identifierLabel != null) {
            visitStatement(statement.statement(0));
        }
        else if(statement.SWITCH() != null) {
            NncUtils.forEach(
                    statement.switchBlockStatementGroup(),
                    this::visitSwitchBlockStatementGroup
            );
        }
        else if(statement.SYNCHRONIZED() != null) {
            visitBlock(statement.block());
        }
        else if(statement.switchExpression() != null) {
            visitSwitchExpression(statement.switchExpression());
        }
    }


    private void visitSwitchExpression(SwitchExpressionContext switchExpression) {
        NncUtils.forEach(
                switchExpression.switchLabeledRule(),
                this::visitSwitchLabeledRule
        );
    }

    private void visitSwitchLabeledRule(SwitchLabeledRuleContext switchLabeledRule) {
        var outcome = switchLabeledRule.switchRuleOutcome();
        if(outcome.block() != null) {
            visitBlock(outcome.block());
        }
        else {
            NncUtils.forEach(
                    outcome.blockStatement(),
                    this::visitBlockStatement
            );
        }
    }

    private void visitCatchClause(CatchClauseContext catchClause) {
        visitBlock(catchClause.block());
    }

    private void visitSwitchBlockStatementGroup(SwitchBlockStatementGroupContext switchBlockStatementGroup) {
        NncUtils.forEach(
                switchBlockStatementGroup.blockStatement(),
                this::visitBlockStatement
        );
    }


    private void visitLambdaExpression(LambdaExpressionContext lambda) {
        if(lambda.lambdaBody().expression() != null) {
            return;
        }
        else {
            visitBlock(lambda.lambdaBody().block());
        }
    }

    private void visitBlock(BlockContext block) {
        NncUtils.forEach(block.blockStatement(), this::visitBlockStatement);
    }

    private void visitBlockStatement(BlockStatementContext blockStatement) {
        if(blockStatement.statement() != null) {
            visitStatement(blockStatement.statement());
        }
    }

}
