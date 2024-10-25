package org.metavm.expression;

import lombok.extern.slf4j.Slf4j;
import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.jetbrains.annotations.NotNull;
import org.metavm.entity.IEntityContext;
import org.metavm.expression.antlr.MetaVMLexer;
import org.metavm.expression.antlr.MetaVMParser;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.query.OperatorTypes;
import org.metavm.object.type.*;
import org.metavm.util.Constants;
import org.metavm.util.Instances;
import org.metavm.util.InternalException;
import org.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.List;

import static java.util.Objects.requireNonNull;

@Slf4j
public class ExpressionParser {

    public static Expression parse(@NotNull Klass type, @NotNull String expression, @NotNull IEntityContext entityContext) {
        return parse(expression, TypeParsingContext.create(type, entityContext));
    }

    public static Expression parse(@NotNull String expression, @NotNull ParsingContext context) {
        return parse(expression, null, context);
    }

    public static Expression parse(@NotNull String expression, @Nullable Type assignedType, @NotNull ParsingContext context) {
        try {
            return new ExpressionParser(expression, context).parse(assignedType);
        }
        catch (Exception e) {
            throw new InternalException("fail to parse expression " + expression, e);
        }
    }

    private final String expression;
    private final MetaVMParser parser;
    private final ParsingContext context;

    public ExpressionParser(String expression, ParsingContext context) {
        this.expression = expression;
        this.context = context;
        CharStream input = CharStreams.fromString(expression);
        parser = new MetaVMParser(new CommonTokenStream(new MetaVMLexer(input)));
        parser.setErrorHandler(new BailErrorStrategy());
    }

    public Expression parse(@Nullable Type assignedType) {
        try {
            Expression expression = antlrPreparse();
            return resolve(expression, assignedType);
        }
        catch (ParseCancellationException e) {
            throw new InternalException("fail to parse expression: " + expression, e);
        }
    }

    private Expression resolve(Expression expression, @Nullable Type assignedType) {
        return ExpressionResolverV2.resolve(expression, assignedType, context);
    }

    Expression antlrPreparse() {
        return parse(parser.expression());
    }

    private Expression parse(MetaVMParser.ExpressionContext expression) {
        if (expression.primary() != null) {
            return preParsePrimary(expression.primary());
        } else if (expression.DOT() != null && !expression.typeType().isEmpty()) {
            return parseStaticField(expression);
        } else if (expression.bop != null) {
            return parseBop(expression);
        } else if (expression.LBRACK() != null) {
            return parseArrayAccess(expression);
        } else if (expression.list() != null) {
            return parseArray(expression.list());
        } else if (expression.prefix != null) {
            return parsePrefix(expression);
        } else if (expression.allMatch() != null) {
            return parseAllMatch(expression.allMatch());
        } else if (expression.AS() != null) {
            return parseAs(expression);
        } else if (expression.methodCall() != null) {
            return parseMethodCall(expression.methodCall());
        } else if(expression.LT().size() == 2 || expression.GT().size() >= 2)
            return parseShift(expression);
        else {
            throw new ExpressionParsingException();
        }
    }

    private Expression parseShift(MetaVMParser.ExpressionContext ctx) {
        var left = parse(ctx.expression(0));
        var right = parse(ctx.expression(1));
        BinaryOperator op;
        if(ctx.LT().size() == 2)
            op = BinaryOperator.LEFT_SHIFT;
        else if(ctx.GT().size() == 2)
            op = BinaryOperator.RIGHT_SHIFT;
        else if(ctx.GT().size() == 3)
            op = BinaryOperator.UNSIGNED_RIGHT_SHIFT;
        else
            throw new IllegalStateException("Cannot resolve operator for expression: " + ctx.getText());
        return new BinaryExpression(op, left, right);
    }

    private Expression parseAs(MetaVMParser.ExpressionContext expression) {
        return new AsExpression(
                parse(expression.expression(0)),
                expression.IDENTIFIER().getText()
        );
    }

    private Expression parseAllMatch(MetaVMParser.AllMatchContext allMatch) {
        return new AllMatchExpression(
                parse(allMatch.expression(0)),
                parse(allMatch.expression(1))
        );
    }

    private Expression parseMethodCall(MetaVMParser.MethodCallContext methodCall) {
        if (methodCall.identifier().IDENTIFIER() != null) {
            return new FunctionExpression(
                    Func.getByName(methodCall.identifier().IDENTIFIER().getText()),
                    methodCall.expressionList() != null ?
                            parseExpressionList(methodCall.expressionList())
                            : List.of()
            );
        } else {
            throw new ExpressionParsingException();
        }
    }

    private List<Expression> parseExpressionList(MetaVMParser.ExpressionListContext expressionList) {
        return NncUtils.map(expressionList.expression(), this::parse);
    }

    private Expression parsePrefix(MetaVMParser.ExpressionContext expression) {
        var operator = switch (expression.prefix.getType()) {
            case MetaVMParser.ADD -> UnaryOperator.POS;
            case MetaVMParser.SUB -> UnaryOperator.NEG;
            case MetaVMParser.BANG -> UnaryOperator.NOT;
            case MetaVMParser.TILDE -> UnaryOperator.BITWISE_COMPLEMENT;
            default -> throw new IllegalStateException("Unexpected prefix: " + expression.prefix.getTokenIndex());
        };
        return new UnaryExpression(operator, parse(expression.expression(0)));
    }

    private ArrayExpression parseArray(MetaVMParser.ListContext list) {
        return new ArrayExpression(
                list.expressionList() != null ?
                        parseExpressionList(list.expressionList()) : List.of(),
                Types.getAnyArrayType()
        );
    }

    private StaticPropertyExpression parseStaticField(MetaVMParser.ExpressionContext expression) {
        var klass = ((ClassType) parseTypeType(expression.typeType(0))).resolve();
        String identifier = expression.identifier().IDENTIFIER().getText();
        Field field = identifier.startsWith(Constants.ID_PREFIX) ?
                klass.getField(Id.parse(identifier.substring(Constants.ID_PREFIX.length()))) :
                klass.getFieldByName(identifier);
        return new StaticPropertyExpression(field.getRef());
    }

    private Expression parseArrayAccess(MetaVMParser.ExpressionContext expression) {
        return new ArrayAccessExpression(
                parse(expression.expression(0)),
                parse(expression.expression(1))
        );
    }

    private Expression preParsePrimary(MetaVMParser.PrimaryContext primary) {
        if (primary.LPAREN() != null) {
            return parse(primary.expression());
        } else if (primary.THIS() != null) {
            return new VariableExpression("this");
        } else if (primary.literal() != null) {
            return preParseLiteral(primary.literal());
        } else if (primary.identifier() != null) {
            return parseIdentifier(primary.identifier());
        } else if(primary.CLASS() != null) {
            return new TypeLiteralExpression(parseTypeType(primary.typeTypeOrVoid()));
        } else {
            throw new ExpressionParsingException();
        }
    }

    private Expression parseBop(MetaVMParser.ExpressionContext expression) {
        var bop = expression.bop;
        return switch (bop.getType()) {
            case MetaVMParser.DOT -> {
                if (expression.identifier() != null) {
                    yield new VariablePathExpression(
                            parse(expression.expression(0)),
                            (VariableExpression) parseIdentifier(expression.identifier())
                    );
                } else {
                    throw new ExpressionParsingException();
                }
            }
            case MetaVMParser.INSTANCEOF -> new InstanceOfExpression(
                    parse(expression.expression(0)),
                    parseTypeType(expression.typeType(0))
            );
            case MetaVMParser.QUESTION -> ConditionalExpression.create(
                    parse(expression.expression(0)),
                    parse(expression.expression(1)),
                    parse(expression.expression(2))
            );
            default -> new BinaryExpression(
                    BinaryOperator.getByOp(bop.getText(), OperatorTypes.BINARY),
                    parse(expression.expression(0)),
                    parse(expression.expression(1))
            );
        };
    }

    private Type parseTypeType(MetaVMParser.TypeTypeOrVoidContext typeType) {
        if(typeType.VOID() != null)
            return PrimitiveType.voidType;
        else
            return parseTypeType(typeType.typeType());
    }

    private Type parseTypeType(MetaVMParser.TypeTypeContext typeType) {
        if(typeType.parType() != null)
            return parseTypeType(typeType.parType().typeType());
        if(typeType.ANY() != null)
            return Types.getAnyType();
        if(typeType.NEVER() != null)
            return Types.getNeverType();
        if(typeType.LBRACK() != null)
            return new UncertainType(parseTypeType(typeType.typeType(0)), parseTypeType(typeType.typeType(1)));
        if(typeType.primitiveType() != null)
            return parsePrimitiveType(typeType.primitiveType());
        if (typeType.classOrInterfaceType() != null)
            return parseClassType(typeType.classOrInterfaceType());
        if (!typeType.BITOR().isEmpty())
            return new UnionType(NncUtils.mapUnique(typeType.typeType(), this::parseTypeType)).flatten();
        if (!typeType.BITAND().isEmpty())
            return new IntersectionType(NncUtils.mapUnique(typeType.typeType(), this::parseTypeType)).flatten();
        if(typeType.ARROW() != null) {
            List<Type> paramTypes = typeType.typeList() != null ?
                    NncUtils.map(typeType.typeList().typeType(), this::parseTypeType) : List.of();
            return new FunctionType(paramTypes, parseTypeType(typeType.typeType(0)));
        }
        if(typeType.arrayKind() != null)
            return new ArrayType(parseTypeType(typeType.typeType(0)), parseArrayKind(typeType.arrayKind()));
        throw new ExpressionParsingException();
    }

    private ClassType parseClassType(MetaVMParser.ClassOrInterfaceTypeContext ctx) {
        var identifier = ctx.qualifiedName();
        String name = identifier.getText();
        var colonIdx = name.indexOf(':');
        if(colonIdx >= 0)
            name = name.substring(0, colonIdx);
        Klass klass;
        if (name.startsWith(Constants.ID_PREFIX))
            klass = context.getTypeDefProvider().getKlass(Id.parse(Constants.removeIdPrefix(name)));
        else
            klass = requireNonNull(context.getTypeDefProvider().findKlassByName(name));
        if(ctx.typeArguments() != null) {
            var typeArgs = NncUtils.map(ctx.typeArguments().typeType(), this::parseTypeType);
            klass = klass.getParameterized(typeArgs);
        }
        return klass.getType();
    }

    private PrimitiveType parsePrimitiveType(MetaVMParser.PrimitiveTypeContext ctx) {
        if(ctx.BOOLEAN() != null)
            return PrimitiveType.booleanType;
        if(ctx.STRING() != null)
            return PrimitiveType.stringType;
        if(ctx.LONG() != null)
            return PrimitiveType.longType;
        if(ctx.CHAR() != null)
            return PrimitiveType.charType;
        if(ctx.DOUBLE() != null)
            return PrimitiveType.doubleType;
        if(ctx.PASSWORD() != null)
            return PrimitiveType.passwordType;
        if(ctx.TIME() != null)
            return PrimitiveType.timeType;
        if(ctx.VOID() != null)
            return PrimitiveType.voidType;
        if(ctx.NULL_LITERAL() != null)
            return PrimitiveType.nullType;
        else
            throw new IllegalStateException("Unrecognized primitive type: " + ctx.getText());

    }

    private ArrayKind parseArrayKind(MetaVMParser.ArrayKindContext ctx) {
        if(ctx.R() != null)
            return ArrayKind.READ_ONLY;
        if(ctx.LBRACK() != null)
            return ArrayKind.READ_WRITE;
        if(ctx.C() != null)
            return ArrayKind.CHILD;
        if(ctx.V() != null)
            return ArrayKind.VALUE;
        throw new IllegalStateException("Unrecognized array kind: " + ctx.getText());
    }

    private Expression preParseLiteral(MetaVMParser.LiteralContext literal) {
        if (literal.STRING_LITERAL() != null) {
            return parseStringLiteral(literal.STRING_LITERAL());
        } else if (literal.CHAR_LITERAL() != null) {
            return parseCharLiteral(literal.CHAR_LITERAL());
        } else if (literal.integerLiteral() != null) {
            String intText = literal.integerLiteral().getText();
            return new ConstantExpression(
                    Instances.longInstance(Long.parseLong(intText))
            );
        } else if (literal.floatLiteral() != null) {
            return new ConstantExpression(
                    Instances.doubleInstance(Double.parseDouble(literal.getText()))
            );
        } else if (literal.BOOL_LITERAL() != null) {
            return new ConstantExpression(
                    Instances.booleanInstance(Boolean.parseBoolean(literal.getText()))
            );
        } else if (literal.NULL_LITERAL() != null) {
            return new ConstantExpression(Instances.nullInstance());
        } else if(literal.NEVER() != null)
            return new NeverExpression();
        else {
            throw new ExpressionParsingException();
        }
    }

    private Expression parseStringLiteral(TerminalNode stringLiteral) {
        return new ConstantExpression(
                Instances.stringInstance(Expressions.deEscapeDoubleQuoted(stringLiteral.getText()))
        );
    }

    private Expression parseSingleQuoteLiteral(TerminalNode singleQuoteStringLiteral) {
        return new ConstantExpression(
                Instances.stringInstance(Expressions.deEscapeSingleQuoted(singleQuoteStringLiteral.getText()))
        );
    }

    private Expression parseCharLiteral(TerminalNode charLiteral) {
        return new ConstantExpression(
                Instances.charInstance(Expressions.deEscapeChar(charLiteral.getText()))
        );
    }

    private Expression parseIdentifier(MetaVMParser.IdentifierContext identifier) {
        if (identifier.IDENTIFIER() != null) {
            String text = identifier.IDENTIFIER().getText();
            if (text.startsWith(Constants.ID_PREFIX)) {
                return new ConstantExpression(context.getInstanceProvider().get(
                        Id.parse(text.substring(Constants.ID_PREFIX.length()))).getReference());
            } else {
                return new VariableExpression(identifier.IDENTIFIER().getText());
            }
        } else {
            throw new ExpressionParsingException();
        }
    }

}
