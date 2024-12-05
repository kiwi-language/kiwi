package org.metavm.asm;

import org.antlr.v4.runtime.Token;
import org.metavm.asm.antlr.AssemblyParser;
import org.metavm.entity.StdKlass;
import org.metavm.entity.natives.StdFunction;
import org.metavm.expression.Expression;
import org.metavm.expression.Expressions;
import org.metavm.flow.*;
import org.metavm.object.type.*;
import org.metavm.util.Instances;
import org.metavm.util.InternalException;
import org.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;
import static org.metavm.asm.Assembler.parseArrayKind;

class AsmExpressionResolver {

    private final Code code;
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
        this.code = callable.getCallable().getCode();
        this.parseTypeFunc = parseTypeFunc;
        this.parseClassTypeFunc = parseClassTypeFunc;
        this.codeGenerator = codeGenerator;
        this.findKlassFunc = findKlassFunc;
    }

    public Type resolve(AssemblyParser.ExpressionContext ctx) {
        try {
            return resolve0(ctx);
        } catch (Exception e) {
            throw new InternalException("Failed to resolve expression: " + ctx.getText(), e);
        }
    }

    private Type resolve0(AssemblyParser.ExpressionContext ctx) {
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

    private Type resolvePostfix(AssemblyParser.ExpressionContext operand, Token postfix) {
        var p = postfix.getType();
        var type = resolve0(operand);
        if(p == AssemblyParser.BANGBANG) {
            Nodes.nonNull(code);
            return type.getUnderlyingType();
        }
        throw new IllegalStateException("Unrecognized postfix " + postfix.getText());
    }

    private Type resolveSelect(AssemblyParser.SelectContext ctx) {
        var klass = requireNonNull(findKlass(ctx.qualifiedName().getText()));
        var indexName = ctx.IDENTIFIER().getText();
        var index = requireNonNull(klass.findIndex(i -> i.getName().equals(indexName)),
                () -> "Cannot find index with name " + indexName + " class " + klass.getTypeDesc());
        NncUtils.map(ctx.expression(), this::resolve0);
        if (ctx.SELECT() != null) {
            Nodes.select(index, code);
            return new ClassType(null, StdKlass.arrayList.get(), List.of(index.getDeclaringType().getType()));
        }
        else {
            Nodes.selectFirst(index, code);
            return Types.getNullableType(index.getDeclaringType().getType());
        }
    }

    private Type resolveCast(AssemblyParser.ExpressionContext operand, AssemblyParser.TypeTypeContext type) {
        var operandType = resolve0(operand);
        var castType = parseType(type);
        if (operandType instanceof PrimitiveType t1 && castType instanceof PrimitiveType t2) {
            var kind1 = t1.getKind();
            var kind2 = t2.getKind();
            if (kind1 == PrimitiveKind.DOUBLE) {
                if (kind2 == PrimitiveKind.FLOAT)
                    Nodes.doubleToFloat(code);
                else if (kind2 == PrimitiveKind.LONG)
                    Nodes.doubleToLong(code);
                else if (kind2 == PrimitiveKind.INT)
                    Nodes.doubleToInt(code);
                else if (kind2 == PrimitiveKind.SHORT)
                    Nodes.doubleToShort(code);
                else if (kind2 == PrimitiveKind.BYTE)
                    Nodes.doubleToByte(code);
                else if (kind2 == PrimitiveKind.CHAR)
                    Nodes.doubleToChar(code);
            } else if (kind1 == PrimitiveKind.FLOAT) {
                if (kind2 == PrimitiveKind.DOUBLE)
                    Nodes.floatToDouble(code);
                else if (kind2 == PrimitiveKind.LONG)
                    Nodes.floatToLong(code);
                else if (kind2 == PrimitiveKind.INT)
                    Nodes.floatToInt(code);
                else if (kind2 == PrimitiveKind.SHORT)
                    Nodes.floatToShort(code);
                else if (kind2 == PrimitiveKind.BYTE)
                    Nodes.floatToByte(code);
                else if (kind2 == PrimitiveKind.CHAR)
                    Nodes.floatToChar(code);
            } else if (kind1 == PrimitiveKind.LONG) {
                if (kind2 == PrimitiveKind.DOUBLE)
                    Nodes.longToDouble(code);
                else if (kind2 == PrimitiveKind.FLOAT)
                    Nodes.longToFloat(code);
                else if (kind2 == PrimitiveKind.INT)
                    Nodes.longToInt(code);
                else if (kind2 == PrimitiveKind.SHORT)
                    Nodes.longToShort(code);
                else if (kind2 == PrimitiveKind.BYTE)
                    Nodes.longToByte(code);
                else if (kind2 == PrimitiveKind.CHAR)
                    Nodes.longToChar(code);
            } else if (kind1 == PrimitiveKind.INT) {
                if (kind2 == PrimitiveKind.DOUBLE)
                    Nodes.intToDouble(code);
                else if (kind2 == PrimitiveKind.FLOAT)
                    Nodes.intToFloat(code);
                else if (kind2 == PrimitiveKind.LONG)
                    Nodes.intToLong(code);
                else if (kind2 == PrimitiveKind.SHORT)
                    Nodes.intToShort(code);
                else if (kind2 == PrimitiveKind.BYTE)
                    Nodes.intToByte(code);
                else if (kind2 == PrimitiveKind.CHAR)
                    Nodes.intToChar(code);
            } else if (kind1 == PrimitiveKind.SHORT) {
                if (kind2 == PrimitiveKind.DOUBLE)
                    Nodes.intToDouble(code);
                else if (kind2 == PrimitiveKind.FLOAT)
                    Nodes.intToFloat(code);
                else if (kind2 == PrimitiveKind.LONG)
                    Nodes.intToLong(code);
                else if (kind2 == PrimitiveKind.BYTE)
                    Nodes.intToByte(code);
                else if (kind2 == PrimitiveKind.CHAR)
                    Nodes.intToChar(code);
            } else if (kind1 == PrimitiveKind.BYTE) {
                if (kind2 == PrimitiveKind.DOUBLE)
                    Nodes.intToDouble(code);
                else if (kind2 == PrimitiveKind.FLOAT)
                    Nodes.intToFloat(code);
                else if (kind2 == PrimitiveKind.LONG)
                    Nodes.intToLong(code);
                else if (kind2 == PrimitiveKind.SHORT)
                    Nodes.intToShort(code);
                else if (kind2 == PrimitiveKind.CHAR)
                    Nodes.intToChar(code);
            } else if (kind1 == PrimitiveKind.CHAR) {
                if (kind2 == PrimitiveKind.DOUBLE)
                    Nodes.intToDouble(code);
                else if (kind2 == PrimitiveKind.FLOAT)
                    Nodes.intToFloat(code);
                else if (kind2 == PrimitiveKind.LONG)
                    Nodes.intToLong(code);
                else if (kind2 == PrimitiveKind.SHORT)
                    Nodes.intToShort(code);
                else if (kind2 == PrimitiveKind.BYTE)
                    Nodes.intToByte(code);
            }
        }
        else
            Nodes.cast(castType, code);
        return castType;
    }

    private Type resolveAllocate(AssemblyParser.AllocatorContext allocator) {
        var type = (ClassType) parseClassType(allocator.classOrInterfaceType());
        var field2expression = new HashMap<String, AssemblyParser.ExpressionContext>();
        if(allocator.allocatorFieldList() != null) {
            var fields = allocator.allocatorFieldList().allocatorField();
            for (AssemblyParser.AllocatorFieldContext field : fields) {
                field2expression.put(field.IDENTIFIER().getText(), field.expression());
            }
        }
        type.foreachField(field -> {
            var fieldValue = field2expression.get(field.getName());
            if(fieldValue != null)
                resolve0(fieldValue);
            else {
                var value = Objects.requireNonNull(Instances.getDefaultValue(field.getType()),
                        () -> "Value for field '" + field.getName() + "' is missing in the allocate expression");
                Nodes.loadConstant(value, code);
            }
        });
        new AddObjectNode(
                code.nextNodeName("addObject"),
                false,
                type,
                code.getLastNode(),
                code
        );
        return type;
    }

    private Type resolveLambdaExpression(AssemblyParser.LambdaExpressionContext ctx) {
        var returnType = parseType(ctx.typeTypeOrVoid());
        var lambda = new Lambda(null, List.of(), returnType, this.code.getFlow());
        var params = parseParameterList(ctx.lambdaParameters().formalParameterList(), lambda);
        lambda.setParameters(params);
        var code = lambda.getCode();
        var asmLambda = new AsmLambda((AsmCallable) codeGenerator.scopeNotNull(), lambda);
        params.forEach(p -> asmLambda.declareVariable(p.getName(), p.getType()));
        codeGenerator.enterScope(asmLambda);
        codeGenerator.processBlock(ctx.lambdaBody().block(), code);
        codeGenerator.exitScope();
        if (ctx.typeTypeOrVoid().VOID() != null)
            Nodes.voidRet(code);
        Nodes.lambda(lambda, this.code);
        return lambda.getFunctionType();
    }

    private Type resolveNew(AssemblyParser.ExpressionContext ctx) {
        var creator = ctx.creator();
        if(creator.arrayKind() != null) {
            var elementType = parseType(creator.typeType());
            var arrayKind = parseArrayKind(creator.arrayKind());
            var type = new ArrayType(elementType, arrayKind);
            Nodes.newArray(type, code);
            return type;
        }
        else {
            var type = (ClassType) parseClassType(creator.classOrInterfaceType());
            List<AssemblyParser.ExpressionContext> arguments =
                    NncUtils.getOrElse(
                            creator.arguments().expressionList(),
                            AssemblyParser.ExpressionListContext::expression,
                            List.of()
                    );
            List<Type> typeArgs = creator.typeArguments() != null ?
                    NncUtils.map(creator.typeArguments().typeType(), this::parseType) : List.of();
            var argTypes = NncUtils.map(arguments, this::resolve0);
            var constructor = type.resolveMethod(
                    type.getKlass().getName(), argTypes, typeArgs, false
            );
            Nodes.newObject(code, constructor, ctx.UNEW() != null, ctx.ENEW() != null);
            return type;
        }
    }

    private Type resolveShift(AssemblyParser.ExpressionContext ctx) {
        var type = resolve0(ctx.expression(0));
        resolve0(ctx.expression(1));
        if(ctx.LT().size() == 2)
            Nodes.shiftLeft(type, code);
        else if(ctx.GT().size() == 2)
            Nodes.shiftRight(type, code);
        else if(ctx.GT().size() == 3)
            Nodes.unsignedShiftRight(type, code);
        else
            throw new IllegalStateException("Invalid shift expression: " + ctx.getText());
        return Types.getLongType();
    }

    private Type resolvePrimary(AssemblyParser.PrimaryContext ctx) {
        if(ctx.LPAREN() != null)
            return resolve0(ctx.expression());
        if(ctx.THIS() != null)
            return getThis();
        if(ctx.literal() != null)
            return resolveLiteral(ctx.literal());
        if(ctx.IDENTIFIER() != null)
            return resolveIdentifier(ctx.IDENTIFIER().getText());
        throw new IllegalStateException("Unrecognized expression: " + ctx.getText());
    }

    private Type resolveIdentifier(String identifier) {
        return loadVariable(callable.resolveVariable(identifier));
    }

    private Type loadVariable(AsmVariable v) {
        var cIdx = v.getContextIndex(callable);
        if(cIdx == -1)
            Nodes.load(v.index(), v.type(), code);
        else
            Nodes.loadContextSlot(cIdx, v.index(), v.type(), code);
        return v.type();
    }

    private Type resolveLiteral(AssemblyParser.LiteralContext literal) {
        var text = literal.getText();
        org.metavm.object.instance.core.Value value;
        if(literal.integerLiteral() != null) {
            if(text.endsWith("l") || text.endsWith("L"))
                value = Instances.longInstance(Long.parseLong(text.substring(0, text.length() - 1)));
            else
                value = Instances.intInstance(Integer.parseInt(text));
        } else if(literal.floatLiteral() != null) {
            if (text.endsWith("f") || text.endsWith("F"))
                value = Instances.floatInstance(Float.parseFloat(text.substring(0, text.length() - 1)));
            else if (text.endsWith("d") || text.endsWith("D"))
                value = Instances.doubleInstance(Double.parseDouble(text.substring(0, text.length() - 1)));
            else
                value = Instances.doubleInstance(Double.parseDouble(text));
        } else if (literal.BOOL_LITERAL() != null)
            value = Instances.intInstance(Boolean.parseBoolean(text));
        else if (literal.CHAR_LITERAL() != null)
            value = Instances.intInstance(Expressions.deEscapeChar(text));
        else if(literal.STRING_LITERAL() != null)
            value = Instances.stringInstance(Expressions.deEscapeDoubleQuoted(text));
        else if(literal.NULL() != null)
            value = Instances.nullInstance();
        else
            throw new IllegalStateException("Unrecognized literal: " + text);
        Nodes.loadConstant(value, code);
        return value.getType();
    }

    private Type resolveBop(AssemblyParser.ExpressionContext ctx) {
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
        var type = resolve0(ctx.expression(0));
        resolve0(ctx.expression(1));
        return switch (bop) {
            case AssemblyParser.ADD -> {
                Nodes.add(type, code);
                yield type;
            }
            case AssemblyParser.SUB -> {
                Nodes.sub(type, code);
                yield type;
            }
            case AssemblyParser.MUL -> {
                Nodes.mul(type, code);
                yield type;
            }
            case AssemblyParser.DIV -> {
                Nodes.div(type, code);
                yield type;
            }
            case AssemblyParser.BITOR -> {
                Nodes.bitOr(type, code);
                yield type;
            }
            case AssemblyParser.BITAND -> {
                Nodes.bitAnd(type, code);
                yield type;
            }
            case AssemblyParser.CARET -> {
                Nodes.bitXor(type, code);
                yield type;
            }
            case AssemblyParser.AND -> {
                Nodes.intBitAnd(code);
                yield Types.getBooleanType();
            }
            case AssemblyParser.OR -> {
                Nodes.intBitOr(code);
                yield Types.getBooleanType();
            }
            case AssemblyParser.MOD -> {
                Nodes.rem(type, code);
                yield type;
            }
            case AssemblyParser.EQUAL -> {
                Nodes.compareEq(type, code);
                yield Types.getBooleanType();
            }
            case AssemblyParser.NOTEQUAL -> {
                Nodes.compareNe(type, code);
                yield Types.getBooleanType();
            }
            case AssemblyParser.GE -> {
                Nodes.compareGe(type, code);
                yield Types.getBooleanType();
            }
            case AssemblyParser.GT -> {
                Nodes.compareGt(type, code);
                yield Types.getBooleanType();
            }
            case AssemblyParser.LT -> {
                Nodes.compareLt(type, code);
                yield Types.getBooleanType();
            }
            case AssemblyParser.LE -> {
                Nodes.compareLe(type, code);
                yield Types.getBooleanType();
            }
            default -> throw new IllegalStateException("Unrecognized operator: " + ctx.bop.getText());
        };
    }

    private Type resolveAssignment(AssemblyParser.ExpressionContext ctx) {
        var assigned = ctx.expression(0);
        var assignment = ctx.expression(1);
        if(assigned.primary() != null && assigned.primary().IDENTIFIER() != null) {
            var v = callable.resolveVariable(assigned.primary().IDENTIFIER().getText());
            var cIdx = v.getContextIndex(callable);
            resolve0(ctx.expression(1));
            Nodes.dup(code);
            if(cIdx == -1)
                Nodes.store(v.index(), code);
            else
                Nodes.storeContextSlot(cIdx, v.index(), code);
            return v.type();
        }
        else if(assigned.bop.getType() == AssemblyParser.DOT && assigned.IDENTIFIER() != null) {
            var qualifierCtx = assigned.expression(0);
            var fieldName = assigned.IDENTIFIER().getText();
            var klass = findKlass(qualifierCtx.getText());
            if(klass != null) {
                var field = klass.getStaticFieldByName(fieldName);
                resolve0(ctx.expression(1));
                Nodes.dup(code);
                Nodes.setStatic(field, code);
                return field.getType();
            }
            else {
                var qualifierType = (ClassType) resolve0(qualifierCtx);
                resolve0(ctx.expression(1));
                Nodes.dupX1(code);
                var field = qualifierType.getFieldByName(fieldName);
                Nodes.setField(field, code);
                return field.getType();
            }
        }
        else if (assigned.LBRACK() != null) {
            var arrayType = (ArrayType) resolve0(assigned.expression(0));
            resolve0(assigned.expression(1));
            resolve0(assignment);
            Nodes.dupX2(code);
            Nodes.setElement(code);
            return arrayType.getElementType();
        }
        else
            throw new IllegalStateException("Invalid assignment expression: " + ctx.getText());
    }

    private Type resolveMethodCall(AssemblyParser.ExpressionContext qualifier, AssemblyParser.MethodCallContext methodCall) {
        ClassType type;
        var methodName = methodCall.IDENTIFIER().getText();
        var targetKlass = findKlass(qualifier.getText());
        if (targetKlass != null) {
            type = new ClassType(null, targetKlass, List.of());
        } else {
            type = (ClassType) resolve0(qualifier);
        }
        List<Type> typeArgs = methodCall.typeArguments() != null ?
                NncUtils.map(methodCall.typeArguments().typeType(), this::parseType) : List.of();
        List<Type> argTypes = methodCall.expressionList() != null ?
                NncUtils.map(methodCall.expressionList().expression(), this::resolve0) : List.of();
        var method = type.resolveMethod(methodName, argTypes, typeArgs, false);
        Nodes.methodCall(method, code);
        return method.getReturnType();
    }

    private Type resolveThisCall(@Nullable AssemblyParser.ExpressionListContext expressionList) {
        var methodName = currentKlass().getName();
        var type = getThis();
        List<Type> argTypes = expressionList != null ?
                NncUtils.map(expressionList.expression(), this::resolve0) : List.of();
        var method = type.resolveMethod(methodName, argTypes, List.of(), false);
        Nodes.methodCall(method, code);
        return null;
    }

    private Type resolveSuperCall(@Nullable AssemblyParser.ExpressionListContext expressionList) {
        var methodName = requireNonNull(currentKlass().getSuperType()).getKlass().getName();
        var type = getThis();
        List<Type> argTypes = expressionList != null ?
                NncUtils.map(expressionList.expression(), this::resolve0) : List.of();
        var method = type.resolveMethod(methodName, argTypes, List.of(), false);
        Nodes.methodCall(method, code);
        return null;
    }

    private Type resolveGetStatic(Klass klass, String name) {
        var field = klass.getStaticFieldByName(name);
        Nodes.getStatic(field, code);
        return field.getType();
    }

    private Type resolveGetField(AssemblyParser.ExpressionContext qualifier, String name) {
        var type = resolve0(qualifier);
        if(type instanceof ArrayType && name.equals("length")) {
            Nodes.arrayLength(code.nextNodeName("length"), code);
            return Types.getIntType();
        } else {
            assert type instanceof ClassType;
            var klass = ((ClassType) type);
            var field = klass.getFieldByName(name);
            Nodes.getProperty(field, code);
            return field.getType();
        }
    }

    private Type resolveConditional(AssemblyParser.ExpressionContext condition,
                                    AssemblyParser.ExpressionContext first,
                                    AssemblyParser.ExpressionContext second) {
        var i = callable.nextVariableIndex();
        resolve0(condition);
        var ifNot = Nodes.ifEq(null, code);
        var type1 = resolve0(first);
        Nodes.store(i, code);
        var g = Nodes.goto_(code);
        ifNot.setTarget(Nodes.noop(code));
        var type2 = resolve0(second);
        Nodes.store(i, code);
        g.setTarget(Nodes.noop(code));
        var type = Types.getCompatibleType(type1, type2);
        Nodes.load(i, type, code);
        return type;
    }

    private Type resolveInstanceOf(AssemblyParser.ExpressionContext operand,
                                    AssemblyParser.TypeTypeContext type) {
        resolve0(operand);
        Nodes.instanceOf(parseType(type), code);
        return Types.getBooleanType();
    }

    private Type resolveArrayAccess(AssemblyParser.ExpressionContext array, AssemblyParser.ExpressionContext index) {
        var arrayType = (ArrayType) resolve0(array);
        resolve0(index);
        Nodes.getElement(code);
        return arrayType.getElementType();
    }

    private Type resolvePrefix(AssemblyParser.ExpressionContext operand, Token prefix) {
        var type = resolve0(operand);
         return switch (prefix.getType()) {
            case AssemblyParser.SUB -> {
                Nodes.neg(type, code);
                yield type;
            }
            case AssemblyParser.TILDE -> {
                Nodes.bitNot(type, code);
                yield type;
            }
            case AssemblyParser.BANG -> {
                Nodes.ne(code);
                yield Types.getBooleanType();
            }
            default -> throw new IllegalStateException("Unrecognized operator: " + prefix.getText());
        };
    }

    private Type resolveFunctionCall(String name, AssemblyParser.ArgumentsContext arguments) {
        var v = callable.tryResolveVariable(name);
        var exprListCtx = arguments.expressionList();
        if(v != null && v.type() instanceof FunctionType) {
            var funcType = (FunctionType) loadVariable(v);
            if(exprListCtx != null)
                NncUtils.map(exprListCtx.expression(), this::resolve0);
            Nodes.function(code, funcType);
            return funcType.getReturnType();
        }
        else {
            var func = StdFunction.valueOf(name).get();
            if(exprListCtx != null)
                NncUtils.map(exprListCtx.expression(), this::resolve0);
            Nodes.functionCall(code, func);
            return func.getReturnType();
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
        return ((Method) code.getFlow()).getDeclaringType();
    }

    private ClassType getThis() {
        var cIdx = callable.getMethodContextIndex();
        var thisType = callable.getDeclaringKlass().getType();
        if (cIdx == -1)
            Nodes.load(0, thisType, code);
        else
            Nodes.loadContextSlot(cIdx, 0, thisType, code);
        return thisType;
    }

    private List<Parameter> parseParameterList(@Nullable AssemblyParser.FormalParameterListContext parameterList,
                                               Callable callable) {
        if (parameterList == null)
            return List.of();
        return NncUtils.map(parameterList.formalParameter(), p -> parseParameter(p, callable));
    }

    private Parameter parseParameter(AssemblyParser.FormalParameterContext parameter, Callable callable) {
        var name = parameter.IDENTIFIER().getText();
        var type = parseType(parameter.typeType());
        var existing = callable.findParameter(p -> p.getName().equals(name));
        if (existing != null) {
            existing.setType(type);
            return existing;
        } else {
            return new Parameter(
                    NncUtils.randomNonNegative(),
                    name,
                    type,
                    callable
            );
        }
    }

    private Type getExpressionType(Expression expression) {
        var lastNode = code.getLastNode();
        return lastNode != null ? lastNode.getNextExpressionTypes().getType(expression) : expression.getType();
    }

}
