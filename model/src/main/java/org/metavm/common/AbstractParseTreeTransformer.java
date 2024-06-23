package org.metavm.common;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.*;

public abstract class AbstractParseTreeTransformer extends AbstractParseTreeVisitor<ParseTree> {
    private ParserRuleContext current;

    protected void transformChildren(ParserRuleContext ctx, ParserRuleContext transformed) {
        var parent = current;
        current = ctx;
        for (int i = 0; i < ctx.getChildCount(); i++) {
            var child = ctx.getChild(i).accept(this);
            if (child instanceof TerminalNode terminalNode)
                transformed.addChild(terminalNode);
            else if (child instanceof ParserRuleContext ruleContext)
                transformed.addChild(ruleContext);
            else
                throw new IllegalStateException("Unexpected child type: " + child.getClass().getName());
        }
        current = parent;
    }

    @Override
    public ParseTree visitErrorNode(ErrorNode node) {
        var transformed = new ErrorNodeImpl(node.getSymbol());
        transformed.setParent(getCurrent());
        return transformed;
    }

    @Override
    public ParseTree visitTerminal(TerminalNode node) {
        var transformed = new TerminalNodeImpl(node.getSymbol());
        transformed.setParent(getCurrent());
        return transformed;
    }

    @Override
    protected ParseTree defaultResult() {
        throw new UnsupportedOperationException();
    }

    protected ParserRuleContext getCurrent() {
        return current;
    }
}
