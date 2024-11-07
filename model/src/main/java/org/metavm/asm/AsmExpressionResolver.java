package org.metavm.asm;

import org.antlr.v4.runtime.Token;
import org.metavm.asm.antlr.AssemblyParser;
import org.metavm.entity.natives.StdFunction;
import org.metavm.expression.Expression;
import org.metavm.expression.Expressions;
import org.metavm.flow.*;
import org.metavm.object.type.*;
import org.metavm.util.InternalException;
import org.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;
import static org.metavm.asm.Assembler.parseArrayKind;

class AsmExpressionResolver {

    private final ScopeRT scope;
    private final AsmCallable callable;
    private final Function<AssemblyParser.TypeTypeContext, Type> parseTypeFunc;
    private final Function<AssemblyParser.ClassOrInterfaceTypeContext, Type> parseClassTypeFunc;
    private final AsmCodeGenerator codeGenerator;
    private final Function<String, Klass> findKlassFunc;

    AsmExpressionResolver(Function<AssemblyParser.TypeTypeContext, Type> parseTypeFunc,
                          Function<AssemblyParser.ClassOrInterfaceTypeContext, Type> parseClassTypeFunc,
                          AsmCodeGenerator codeGenerator,
                          Function<String, Klass> findKlassFunc) {
        this.callable = (AsmCallable) codeGenerator.scopeNotNull();
        this.scope = callable.getCallable().getScope();
        this.parseTypeFunc = parseTypeFunc;
        this.parseClassTypeFunc = parseClassTypeFunc;
        this.codeGenerator = codeGenerator;
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
        if(ctx.THIS() != null)
            return resolveThisCall(ctx.expressionList());
        if(ctx.SUPER() != null)
            return resolveSuperCall(ctx.expressionList());
        if(ctx.LT().size() == 2 || ctx.GT().size() >= 2)
            return resolveShift(ctx);
        if (ctx.LBRACK() != null)
            return resolveArrayAccess(ctx.expression(0), ctx.expression(1));
        if (ctx.prefix != null)
            return resolvePrefix(ctx.expression(0), ctx.prefix);
        if(ctx.postfix != null)
            return resolvePostfix(ctx.expression(0), ctx.postfix);
        if (ctx.arguments() != null)
            return resolveFunctionCall(ctx.IDENTIFIER().getText(), ctx.arguments());
        if (ctx.NEW() != null || ctx.UNEW() != null || ctx.ENEW() != null)
            return resolveNew(ctx);
        if (ctx.lambdaExpression() != null)
            return resolveLambdaExpression(ctx.lambdaExpression());
        if (ctx.select() != null)
            return resolveSelect(ctx.select());
        if (ctx.castType != null)
            return resolveCast(ctx.expression(0), ctx.castType);
        if (ctx.allocator() != null)
            return resolveAllocate(ctx.allocator());
        throw new IllegalStateException("Unrecognized expression: " + ctx.getText());
    }

    private Value resolvePostfix(AssemblyParser.ExpressionContext operand, Token postfix) {
        var p = postfix.getType();
        var o = resolve0(operand);
        if(p == AssemblyParser.BANGBANG)
            return new NodeValue(Nodes.nonNull(o, scope));
        throw new IllegalStateException("Unrecognized postfix " + postfix.getText());
    }

    private Value resolveSelect(AssemblyParser.SelectContext ctx) {
        var klass = requireNonNull(findKlass(ctx.qualifiedName().getText()));
        var indexName = ctx.IDENTIFIER().getText();
        var index = requireNonNull(klass.findIndex(i -> i.getName().equals(indexName)),
                () -> "Cannot find index with name " + indexName + " class " + klass.getTypeDesc());
        var fieldValues = NncUtils.map(ctx.expression(), this::resolve0);
        var key = new IndexQueryKey(
                index,
                NncUtils.biMap(index.getFields(), fieldValues, IndexQueryKeyItem::new)
        );
        if (ctx.SELECT() != null)
            return new NodeValue(Nodes.select(index, key, scope));
        else
            return new NodeValue(Nodes.selectFirst(index, key, scope));
    }

    private Value resolveCast(AssemblyParser.ExpressionContext operand, AssemblyParser.TypeTypeContext type) {
        return new NodeValue(Nodes.cast(
                scope.nextNodeName("cast"),
                parseType(type),
                resolve0(operand),
                scope
        ));
    }

    private Value resolveAllocate(AssemblyParser.AllocatorContext allocator) {
        var type = (ClassType) parseClassType(allocator.classOrInterfaceType());
        var fieldParams = new ArrayList<FieldParam>();
        if(allocator.allocatorFieldList() != null) {
            var fields = allocator.allocatorFieldList().allocatorField();
            var klass = type.resolve();
            for (AssemblyParser.AllocatorFieldContext field : fields) {
                fieldParams.add(
                        new FieldParam(
                                klass.getFieldByCode(field.IDENTIFIER().getText()).getRef(),
                                resolve0(field.expression())
                        )
                );
            }
        }
        var node = new AddObjectNode(
                null,
                scope.nextNodeName("addObject"),
                null,
                false,
                false,
                type,
                scope.getLastNode(),
                scope
        );
        fieldParams.forEach(node::addField);
        return new NodeValue(node);
    }

    private Value resolveLambdaExpression(AssemblyParser.LambdaExpressionContext ctx) {
        var params = parseParameterList(ctx.lambdaParameters().formalParameterList(), null);
        var returnType = parseType(ctx.typeTypeOrVoid());
        var lambda = new Lambda(null, params, returnType, scope.getFlow());
        var lambdaScope = lambda.getScope();
        var asmLambda = new AsmLambda((AsmCallable) codeGenerator.scopeNotNull(), lambda);
        params.forEach(p -> asmLambda.declareVariable(p.getName(), p.getType()));
        codeGenerator.enterScope(asmLambda);
        codeGenerator.processBlock(ctx.lambdaBody().block(), lambdaScope);
        codeGenerator.exitScope();
        if (ctx.typeTypeOrVoid().VOID() != null)
            Nodes.ret(scope.nextNodeName("ret"), lambdaScope, null);
        return new NodeValue(Nodes.lambda(lambda, scope));
    }

    private Value resolveNew(AssemblyParser.ExpressionContext ctx) {
        var creator = ctx.creator();
        if(creator.arrayKind() != null) {
            var elementType = parseType(creator.typeType());
            var arrayKind = parseArrayKind(creator.arrayKind());
            var type = new ArrayType(elementType, arrayKind);
            return new NodeValue(Nodes.newArray(
                    scope.nextNodeName("newArray"),
                    null,
                    type,
                    null,
                    null,
                    scope
            ));
        }
        else {
            var type = (ClassType) parseClassType(creator.classOrInterfaceType());
            var targetKlass = type.resolve();
            List<AssemblyParser.ExpressionContext> arguments =
                    NncUtils.getOrElse(
                            creator.arguments().expressionList(),
                            AssemblyParser.ExpressionListContext::expression,
                            List.of()
                    );
            List<Type> typeArgs = creator.typeArguments() != null ?
                    NncUtils.map(creator.typeArguments().typeType(), this::parseType) : List.of();
            var args = NncUtils.map(arguments, this::resolve0);
            var constructor = targetKlass.resolveMethod(
                    targetKlass.getEffectiveTemplate().getName(), NncUtils.map(args, Value::getType), typeArgs, false
            );
            return new NodeValue(new NewObjectNode(
                    NncUtils.randomNonNegative(),
                    scope.nextNodeName("newObject"),
                    null,
                    constructor.getRef(),
                    NncUtils.biMap(constructor.getParameters(), args, (p, a) -> new Argument(NncUtils.randomNonNegative(), p.getRef(), a)),
                    scope.getLastNode(),
                    scope,
                    null,
                    ctx.UNEW() != null,
                    ctx.ENEW() != null
            ));
        }
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
            return resolveIdentifier(ctx.IDENTIFIER().getText());
        throw new IllegalStateException("Unrecognized expression: " + ctx.getText());
    }

    private Value resolveIdentifier(String identifier) {
        return loadVariable(callable.resolveVariable(identifier));
    }

    private Value loadVariable(AsmVariable v) {
        var cIdx = v.getContextIndex(callable);
        if(cIdx == -1)
            return new NodeValue(Nodes.load(v.index(), v.type(), scope));
        else
            return new NodeValue(Nodes.loadContextSlot(cIdx, v.index(), v.type(), scope));
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
        if(bop == AssemblyParser.ASSIGN)
            return resolveAssignment(ctx);
        if(bop == AssemblyParser.QUESTION)
            return resolveConditional(ctx.expression(0), ctx.expression(1), ctx.expression(2));
        if(bop == AssemblyParser.INSTANCEOF)
            return resolveInstanceOf(ctx.expression(0), ctx.typeType());
        if(ctx.methodCall() != null)
            return resolveMethodCall(ctx.expression(0), ctx.methodCall());
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
            default -> throw new IllegalStateException("Unrecognized operator: " + ctx.bop.getText());
        };
        return Values.node(node);
    }

    private Value resolveAssignment(AssemblyParser.ExpressionContext ctx) {
        var assignedCtx = ctx.expression(0);
        var assignment = resolve0(ctx.expression(1));
        if(assignedCtx.primary() != null && assignedCtx.primary().IDENTIFIER() != null) {
            var v = callable.resolveVariable(assignedCtx.primary().IDENTIFIER().getText());
            var cIdx = v.getContextIndex(callable);
            if(cIdx == -1)
                Nodes.store(v.index(), assignment, scope);
            else
                Nodes.storeContextSlot(cIdx, v.index(), assignment, scope);
        }
        else if(assignedCtx.bop.getType() == AssemblyParser.DOT && assignedCtx.IDENTIFIER() != null) {
            var qualifierCtx = assignedCtx.expression(0);
            var fieldName = assignedCtx.IDENTIFIER().getText();
            var klass = findKlass(qualifierCtx.getText());
            if(klass != null) {
                var field = klass.getStaticFieldByName(fieldName);
                Nodes.updateStatic(field, assignment, scope);
            }
            else {
                var qualifier = resolve0(qualifierCtx);
                var qualifierType = (ClassType) qualifier.getType();
                var field = qualifierType.resolve().getFieldByCode(fieldName);
                Nodes.update(qualifier, field, assignment, scope);
            }
        }
        return assignment;
    }

    private Value resolveMethodCall(AssemblyParser.ExpressionContext qualifier, AssemblyParser.MethodCallContext methodCall) {
        List<Value> arguments = methodCall.expressionList() != null ?
                NncUtils.map(methodCall.expressionList().expression(), this::resolve0) : List.of();
        Value self;
        ClassType type;
        var methodName = methodCall.IDENTIFIER().getText();
        var targetKlass = findKlass(qualifier.getText());
        if (targetKlass != null) {
            type = new ClassType(targetKlass, List.of());
            self = null;
        } else {
            self = resolve0(qualifier);
            type = (ClassType) self.getType();
        }
        List<Type> typeArgs = methodCall.typeArguments() != null ?
                NncUtils.map(methodCall.typeArguments().typeType(), this::parseType) : List.of();
        Method method = type.resolve().resolveMethod(methodName, NncUtils.map(arguments, Value::getType), typeArgs, false);
        return new NodeValue(Nodes.methodCall(self, method, arguments, scope));
    }

    private Value resolveThisCall(@Nullable AssemblyParser.ExpressionListContext expressionList) {
        List<Value> arguments = expressionList != null ?
                NncUtils.map(expressionList.expression(), this::resolve0) : List.of();
        var methodName = currentKlass().getName();
        var self = getThis();
        var type = (ClassType) self.getType();
        Method method = type.resolve().resolveMethod(methodName, NncUtils.map(arguments, Value::getType), List.of(), false);
        return new NodeValue(Nodes.methodCall(self, method, arguments, scope));
    }

    private Value resolveSuperCall(@Nullable AssemblyParser.ExpressionListContext expressionList) {
        List<Value> arguments = expressionList != null ?
                NncUtils.map(expressionList.expression(), this::resolve0) : List.of();
        var methodName = requireNonNull(currentKlass().getSuperType()).getKlass().getName();
        var self = getThis();
        var type = (ClassType) self.getType();
        Method method = type.resolve().resolveMethod(methodName, NncUtils.map(arguments, Value::getType), List.of(), false);
        return new NodeValue(Nodes.methodCall(self, method, arguments, scope));
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
        var e = requireNonNull(scope.getLastNode());
        var join = Nodes.join(scope);
        g.setTarget(join);
        var field = FieldBuilder.newBuilder("value", null, join.getKlass(),
                Types.getCompatibleType(result1.getType(), result2.getType())).build();
        new JoinNodeField(field, join, Map.of(g, result1, e, result2));
        return Values.node(Nodes.nodeProperty(join, field, scope));
    }

    private Value resolveInstanceOf(AssemblyParser.ExpressionContext operand,
                                    AssemblyParser.TypeTypeContext type) {
        return Values.node(Nodes.instanceOf(resolve0(operand), parseType(type), scope));
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
        var v = callable.tryResolveVariable(name);
        var exprListCtx = arguments.expressionList();
        List<Value> args = exprListCtx != null ? NncUtils.map(exprListCtx.expression(), this::resolve0) : List.of();
        if(v != null && v.type() instanceof FunctionType) {
            return new NodeValue(Nodes.function(
                    scope.nextNodeName("call"),
                    scope,
                    loadVariable(v),
                    args
            ));
        }
        else {
            var func = StdFunction.valueOf(name).get();
            return Values.node(Nodes.functionCall(scope.nextNodeName("func"),
                    scope, func, NncUtils.biMap(func.getParameters(), args, (param, arg) ->
                            new Argument(null, param.getRef(), arg))));
        }
    }

    private Type parseType(AssemblyParser.TypeTypeOrVoidContext ctx) {
        return ctx.VOID() != null ? Types.getVoidType() : parseTypeFunc.apply(ctx.typeType());
    }

    private Type parseType(AssemblyParser.TypeTypeContext ctx) {
        return parseTypeFunc.apply(ctx);
    }

    private Type parseClassType(AssemblyParser.ClassOrInterfaceTypeContext ctx) {
        return parseClassTypeFunc.apply(ctx);
    }

    private @Nullable Klass findKlass(String name) {
        return findKlassFunc.apply(name);
    }

    private Klass currentKlass() {
        return ((Method) scope.getFlow()).getDeclaringType();
    }

    private Value getThis() {
        var cIdx = callable.getMethodContextIndex();
        var thisType = callable.getDeclaringKlass().getType();
        var node =  cIdx == -1 ?
                Nodes.load(0, thisType, scope) : Nodes.loadContextSlot(cIdx, 0, thisType, scope);
        return new NodeValue(node);
    }

    private List<Parameter> parseParameterList(@Nullable AssemblyParser.FormalParameterListContext parameterList,
                                               @Nullable Callable callable) {
        if (parameterList == null)
            return List.of();
        return NncUtils.map(parameterList.formalParameter(), p -> parseParameter(p, callable));
    }

    private Parameter parseParameter(AssemblyParser.FormalParameterContext parameter, @Nullable Callable callable) {
        var name = parameter.IDENTIFIER().getText();
        var type = parseType(parameter.typeType());
        if (callable != null) {
            var existing = callable.findParameter(p -> p.getName().equals(name));
            if (existing != null) {
                existing.setType(type);
                return existing;
            }
        }
        return new Parameter(
                NncUtils.randomNonNegative(),
                name,
                name,
                type
        );
    }

    private Type getExpressionType(Expression expression) {
        var lastNode = scope.getLastNode();
        return lastNode != null ? lastNode.getNextExpressionTypes().getType(expression) : expression.getType();
    }

}
