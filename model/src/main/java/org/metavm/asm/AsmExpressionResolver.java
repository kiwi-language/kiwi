package org.metavm.asm;

import org.antlr.v4.runtime.Token;
import org.metavm.asm.antlr.AssemblyParser;
import org.metavm.entity.natives.StdFunction;
import org.metavm.expression.Expressions;
import org.metavm.flow.*;
import org.metavm.object.type.*;
import org.metavm.util.InternalException;
import org.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

public class AsmExpressionResolver {

    private final ScopeRT scope;
    private final Function<AssemblyParser.TypeTypeContext, Type> parseTypeFunc;
    private final Function<String, Klass> findKlassFunc;

    public AsmExpressionResolver(ScopeRT scope, Function<AssemblyParser.TypeTypeContext, Type> parseTypeFunc, Function<String, Klass> findKlassFunc) {
        this.scope = scope;
        this.parseTypeFunc = parseTypeFunc;
        this.findKlassFunc = findKlassFunc;
    }

    public Value resolve(AssemblyParser.ExpressionContext ctx) {
        try {
            return resolve0(ctx);
        } catch (Exception e) {
            throw new InternalException("Failed to resolve expression: " + ctx.getText(), e);
        }
    }

    private Value resolve0(AssemblyParser.ExpressionContext ctx) {
        if (ctx.primary() != null)
            return resolvePrimary(ctx.primary());
        if (ctx.bop != null)
            return resolveBop(ctx);
        if(ctx.LT().size() == 2 || ctx.GT().size() >= 2)
            return resolveShift(ctx);
        if (ctx.LBRACK() != null)
            return resolveArrayAccess(ctx.expression(0), ctx.expression(1));
        if (ctx.prefix != null)
            return resolvePrefix(ctx.expression(0), ctx.prefix);
        if (ctx.arguments() != null)
            return resolveFunctionCall(ctx.IDENTIFIER().getText(), ctx.arguments());
        throw new IllegalStateException("Unrecognized expression: " + ctx.getText());
    }

    private Value resolveShift(AssemblyParser.ExpressionContext ctx) {
        var first = resolve0(ctx.expression(0));
        var second = resolve0(ctx.expression(1));
        NodeRT node;
        if(ctx.LT().size() == 2)
            node = Nodes.leftShift(first, second, scope);
        else if(ctx.GT().size() == 2)
            node = Nodes.rightShift(first, second, scope);
        else if(ctx.GT().size() == 3)
            node = Nodes.unsignedRightShift(first, second, scope);
        else
            throw new IllegalStateException("Invalid shift expression: " + ctx.getText());
        return Values.node(node);
    }

    private Value resolvePrimary(AssemblyParser.PrimaryContext ctx) {
        if(ctx.LPAREN() != null)
            return resolve0(ctx.expression());
        if(ctx.THIS() != null)
            return Values.node(scope.getNodeByName("this"));
        if(ctx.literal() != null)
            return resolveLiteral(ctx.literal());
        if(ctx.IDENTIFIER() != null)
            return Values.node(scope.getNodeByName(ctx.IDENTIFIER().getText()));
        throw new IllegalStateException("Unrecognized expression: " + ctx.getText());
    }

    private Value resolveLiteral(AssemblyParser.LiteralContext literal) {
        var text = literal.getText();
        if(literal.integerLiteral() != null)
            return Values.constantLong(Long.parseLong(text));
        if(literal.floatLiteral() != null)
            return Values.constantDouble(Double.parseDouble(text));
        if(literal.BOOL_LITERAL() != null)
            return Values.constantBoolean(Boolean.parseBoolean(text));
        if(literal.CHAR_LITERAL() != null)
            return Values.constantChar(Expressions.deEscapeChar(text));
        if(literal.STRING_LITERAL() != null)
            return Values.constantString(Expressions.deEscapeDoubleQuoted(text));
        if(literal.NULL() != null)
            return Values.nullValue();
        throw new IllegalStateException("Unrecognized literal: " + text);
    }

    private Value resolveBop(AssemblyParser.ExpressionContext ctx) {
        var bop = ctx.bop.getType();
        if(ctx.IDENTIFIER() != null) {
            var qualifier = ctx.expression(0);
            var klass = findKlass(qualifier.getText());
            if(klass != null)
                return resolveGetStatic(klass, ctx.IDENTIFIER().getText());
            else
                return resolveGetField(qualifier, ctx.IDENTIFIER().getText());
        }
        if(bop == AssemblyParser.QUESTION)
            return resolveConditional(ctx.expression(0), ctx.expression(1), ctx.expression(2));
        if(bop == AssemblyParser.INSTANCEOF)
            return resolveInstanceOf(ctx.expression(0), ctx.typeType());
        var first = resolve0(ctx.expression(0));
        var second = resolve0(ctx.expression(1));
        var node = switch (bop) {
            case AssemblyParser.ADD -> Nodes.add(first, second, scope);
            case AssemblyParser.SUB -> Nodes.sub(first, second, scope);
            case AssemblyParser.MUL -> Nodes.mul(first, second, scope);
            case AssemblyParser.DIV -> Nodes.div(first, second, scope);
            case AssemblyParser.BITOR -> Nodes.bitwiseOr(first, second, scope);
            case AssemblyParser.BITAND -> Nodes.bitwiseAnd(first, second, scope);
            case AssemblyParser.CARET -> Nodes.bitwiseXor(first, second, scope);
            case AssemblyParser.AND -> Nodes.and(first, second, scope);
            case AssemblyParser.OR -> Nodes.or(first, second, scope);
            case AssemblyParser.MOD -> Nodes.rem(first, second, scope);
            case AssemblyParser.EQUAL -> Nodes.eq(first, second, scope);
            case AssemblyParser.NOTEQUAL -> Nodes.ne(first, second, scope);
            case AssemblyParser.GE -> Nodes.ge(first, second, scope);
            case AssemblyParser.GT -> Nodes.gt(first, second, scope);
            case AssemblyParser.LT -> Nodes.lt(first, second, scope);
            case AssemblyParser.LE -> Nodes.le(first, second, scope);
            default -> throw new IllegalStateException("Unrecognized operator: " + bop);
        };
        return Values.node(node);
    }

    private Value resolveGetStatic(Klass klass, String name) {
        return Values.node(Nodes.getStatic(klass.getStaticFieldByName(name), scope));
    }

    private Value resolveGetField(AssemblyParser.ExpressionContext qualifier, String name) {
        var i = resolve(qualifier);
        if(i.getType() instanceof ArrayType && name.equals("length"))
            return Values.node(Nodes.arrayLength(scope.nextNodeName("length"), i, scope));
        else {
            var klass = ((ClassType) i.getType()).resolve();
            var field = klass.getFieldByCode(name);
            return Values.node(Nodes.getProperty(i, field, scope));
        }
    }

    private Value resolveConditional(AssemblyParser.ExpressionContext condition,
                                     AssemblyParser.ExpressionContext first,
                                     AssemblyParser.ExpressionContext second) {
        var ifNot = Nodes.ifNot(resolve0(condition), null, scope);
        var result1 = resolve0(first);
        var g = Nodes.goto_(scope);
        ifNot.setTarget(Nodes.noop(scope));
        var result2 = resolve0(second);
        var e = Objects.requireNonNull(scope.getLastNode());
        var join = Nodes.join(scope);
        g.setTarget(join);
        var field = FieldBuilder.newBuilder("value", null, join.getKlass(),
                Types.getCompatibleType(result1.getType(), result2.getType())).build();
        new JoinNodeField(field, join, Map.of(g, result1, e, result2));
        return Values.node(Nodes.nodeProperty(join, field, scope));
    }

    private Value resolveInstanceOf(AssemblyParser.ExpressionContext operand,
                                    AssemblyParser.TypeTypeContext type) {
        return Values.node(Nodes.instanceOf(resolve0(operand), resolveType(type), scope));
    }

    private Value resolveArrayAccess(AssemblyParser.ExpressionContext array, AssemblyParser.ExpressionContext index) {
        return Values.node(Nodes.getElement(resolve0(array), resolve0(index), scope));
    }

    private Value resolvePrefix(AssemblyParser.ExpressionContext operand, Token prefix) {
        var v = resolve0(operand);
        var node = switch (prefix.getType()) {
            case AssemblyParser.SUB -> Nodes.negate(v, scope);
            case AssemblyParser.TILDE -> Nodes.bitwiseComplement(v, scope);
            case AssemblyParser.BANG -> Nodes.not(v, scope);
            default -> throw new IllegalStateException("Unrecognized operator: " + prefix.getText());
        };
        return Values.node(node);
    }

    private Value resolveFunctionCall(String name, AssemblyParser.ArgumentsContext arguments) {
        var func = StdFunction.valueOf(name).get();
        var exprListCtx = arguments.expressionList();
        List<Value> args = exprListCtx != null ? NncUtils.map(exprListCtx.expression(), this::resolve0) : List.of();
        return Values.node(Nodes.functionCall(scope.nextNodeName("func"),
                scope, func, NncUtils.biMap(func.getParameters(), args, (param, arg) ->
                        new Argument(null, param.getRef(), arg))));
    }

    private Type resolveType(AssemblyParser.TypeTypeContext ctx) {
        return parseTypeFunc.apply(ctx);
    }

    private @Nullable Klass findKlass(String name) {
        return findKlassFunc.apply(name);
    }

}
