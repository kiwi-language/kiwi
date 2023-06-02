package tech.metavm.transpile;

import tech.metavm.util.NncUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static tech.metavm.transpile.JavaParser.*;

public class StatementContextWalker {

    public static void walk(
            BlockContext blockContext,
            Predicate<StatementContext> visitor
    ) {
        var stmts = getStatementsFromBlockStatements(blockContext.blockStatement());
        new StatementContextWalker(stmts, visitor).walk();
    }

    public static void walk(
            Iterable<StatementContext> statementContexts,
            Predicate<StatementContext> visitor
    ) {
        new StatementContextWalker(statementContexts, visitor).walk();
    }

    private final Iterable<StatementContext> statementContexts;
    private final Predicate<StatementContext> visitor;

    private StatementContextWalker(Iterable<StatementContext> statementContexts, Predicate<StatementContext> visitor) {
        this.statementContexts = statementContexts;
        this.visitor = visitor;
    }

    private void walk() {
        for (StatementContext statementContext : statementContexts) {
            if(processStatement(statementContext)) {
                return;
            }
        }
    }

    private boolean processStatement(StatementContext statementContext) {
        boolean done = visitor.test(statementContext);
        if(done) {
            return true;
        }
        for (StatementContext child : getChildren(statementContext)) {
            if(processStatement(child)) {
                return true;
            }
        }
        return false;
    }

    private List<StatementContext> getChildren(StatementContext statementContext) {
        if(statementContext.IF() != null || statementContext.FOR() != null || statementContext.WHILE() != null
                || statementContext.WHILE() != null) {
            return statementContext.statement();
        }
        if(statementContext.TRY() != null) {
            List<StatementContext> children = new ArrayList<>(
                    getStatementsFromBlock(statementContext.block())
            );
            children.addAll(
                    NncUtils.flatMap(
                            statementContext.catchClause(),
                            this::getStatementsFromCatchClause
                    )
            );
            if(statementContext.finallyBlock() != null) {
                children.addAll(getStatementsFromBlock(statementContext.finallyBlock().block()));
            }
            return children;
        }
        if(statementContext.SWITCH() != null) {
            return NncUtils.flatMap(
                    statementContext.switchBlockStatementGroup(),
                    this::getStatementsFromSwitchBlock
            );
        }
        if(statementContext.SYNCHRONIZED() != null) {
            return getStatementsFromBlock(statementContext.block());
        }
        if(statementContext.switchExpression() != null) {
            return NncUtils.flatMap(
                    statementContext.switchExpression().switchLabeledRule(),
                    this::getStatementsFromSwitchLabeledRule
            );
        }
        if(statementContext.identifierLabel != null) {
            return List.of(statementContext.statement(0));
        }
        return List.of();
    }

    private List<StatementContext> getStatementsFromBlock(BlockContext blockContext) {
        return getStatementsFromBlockStatements(blockContext.blockStatement());
    }

    private static List<StatementContext> getStatementsFromBlockStatements(List<BlockStatementContext> blockStatementContexts) {
        List<StatementContext> result = new ArrayList<>();
        for (BlockStatementContext blockStmt : blockStatementContexts) {
            if(blockStmt.statement() != null) {
                result.add(blockStmt.statement());
            }
        }
        return result;
    }

    private List<StatementContext> getStatementsFromCatchClause(CatchClauseContext catchClauseContext) {
        return getStatementsFromBlock(catchClauseContext.block());
    }

    private List<StatementContext> getStatementsFromSwitchBlock(SwitchBlockStatementGroupContext switchBlock) {
        return getStatementsFromBlockStatements(switchBlock.blockStatement());
    }

    private List<StatementContext> getStatementsFromSwitchLabeledRule(SwitchLabeledRuleContext switchLabeledRuleContext) {
        var outcome = switchLabeledRuleContext.switchRuleOutcome();
        if(outcome.block() != null) {
            return getStatementsFromBlock(outcome.block());
        }
        else {
            return getStatementsFromBlockStatements(outcome.blockStatement());
        }
    }

}
