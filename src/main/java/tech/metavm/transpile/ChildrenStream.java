package tech.metavm.transpile;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.jetbrains.annotations.Nullable;
import tech.metavm.object.instance.query.PathTree;
import tech.metavm.util.InternalException;
import tech.metavm.util.NncUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import static tech.metavm.transpile.JavaParser.*;

class ChildrenStream {

    private final ParserRuleContext context;
    private int index;

    ChildrenStream(ParserRuleContext context) {
        this.context = context;
    }

    boolean hasNext() {
        return index < context.getChildCount();
    }

    <T extends ParseTree> T find(Class<T> klass) {
        var i = index;
        while (i < context.getChildCount()) {
            var child = context.getChild(i++);
            if(klass.isInstance(child)) {
                index = i + 1;
                return klass.cast(child);
            }
        }
        throw new InternalException("Can not find child of type: " + klass.getName());
    }

    ExpressionContext findExpression() {
        return find(ExpressionContext.class);
    }

    ParseTree next() {
        return context.getChild(index++);
    }

    TerminalNode nextTerminal() {
        return next(TerminalNode.class);
    }

    TerminalNode nextTerminal(int symbolType) {
        var terminal = next(TerminalNode.class);
        NncUtils.requireEquals(symbolType, terminal.getSymbol().getType());
        return terminal;
    }

    ParExpressionContext nextParExpression() {
        return next(ParExpressionContext.class);
    }

    StatementContext nextStatement() {
        return next(StatementContext.class);
    }

    ExpressionContext nextExpression() {
        return next(ExpressionContext.class);
    }

    TypeTypeContext nextTypeType() {
        return next(TypeTypeContext.class);
    }

    <T extends ParseTree> T next(Class<T> klass) {
        var child = context.getChild(index);
        if(klass.isInstance(child)) {
            index++;
            return klass.cast(child);
        }
        else {
            throw new InternalException(
                    "Expecting type: " + klass.getName() + ", actual type: " + child.getClass().getName()
            );
        }
    }

    <T extends ParseTree> @Nullable T nextIf(Class<T> klass) {
        if(isNextInstanceOf(klass)) {
            return context.getChild(klass, index++);
        }
        else {
            return null;
        }
    }


    <T extends ParseTree> List<T> nextList(Class<T> klass) {
        var result = new ArrayList<T>();
        while (isNextInstanceOf(klass)) {
            result.add(next(klass));
        }
        return result;
    }

    <E extends ParserRuleContext, R> R mapIfNextInstanceOf(Class<E> klass, Function<E, R> mapper) {
        return mapIfNextInstanceOf(klass, mapper, () -> null);
    }

    <E extends ParserRuleContext, R> R mapIfNextInstanceOf(Class<E> klass, Function<E, R> mapper, Supplier<R> defaultSupplier) {
        if(isNextInstanceOf(klass)) {
            return mapper.apply(next(klass));
        }
        else {
            return defaultSupplier.get();
        }
    }

    boolean isNextInstanceOf(Class<? extends ParseTree> klass) {
        return klass.isInstance(context.getChild(index));
    }

    boolean skipIfNextInstanceOf(Class<? extends ParseTree> klass) {
        if(isNextInstanceOf(klass)) {
            next();
            return true;
        }
        else {
            return false;
        }
    }

    boolean skipIfNextSymbolOf(int symbolType) {
        var next = context.getChild(index);
        if(next instanceof TerminalNode terminalNode && terminalNode.getSymbol().getType() == symbolType) {
            index++;
            return true;
        }
        return false;
    }

    boolean isNextLiteral(String literal) {
        var next = context.getChild(index);
        return next instanceof TerminalNode terminalNode && literal.equals(terminalNode.getText());
    }

    int countLiteral(String literal) {
        int count = 0;
        for (; index < context.getChildCount(); index++) {
            var child = context.getChild(index);
            if(child instanceof TerminalNode terminalNode && terminalNode.getText().equals(literal)) {
                count++;
            }
        }
        return count;
    }

    public boolean isNextExpression() {
        return isNextInstanceOf(ExpressionContext.class);
    }

    public boolean isNextTypeType() {
        return isNextInstanceOf(TypeTypeContext.class);
    }

    boolean isNextTerminalOf(int symbolType) {
        var next = peek();
        return next instanceof TerminalNode terminalNode
                && terminalNode.getSymbol().getType() == symbolType;
    }

    private ParseTree peek() {
        return context.getChild(index);
    }

    public boolean isNextBlock() {
        return isNextInstanceOf(BlockContext.class);
    }

    public BlockContext nextBlock() {
        return next(BlockContext.class);
    }

    public IdentifierContext nextIdentifier() {
        return next(IdentifierContext.class);
    }

    public boolean isNextIdentifier() {
        return isNextInstanceOf(IdentifierContext.class);
    }

    public <T> List<T> findAll(Class<T> klass) {
        List<T> result = new ArrayList<>();
        while(index < context.getChildCount()) {
            var child = context.getChild(index++);
            if(klass.isInstance(child)) {
                result.add(klass.cast(child));
            }
        }
        return result;
    }

}
