package tech.metavm.expression;

import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.jetbrains.annotations.NotNull;
import tech.metavm.entity.IEntityContext;
import tech.metavm.entity.StandardTypes;
import tech.metavm.expression.antlr.MetaVMLexer;
import tech.metavm.expression.antlr.MetaVMParser;
import tech.metavm.object.instance.core.Id;
import tech.metavm.object.instance.query.OperatorTypes;
import tech.metavm.object.type.ClassType;
import tech.metavm.object.type.Klass;
import tech.metavm.object.type.Field;
import tech.metavm.object.type.Type;
import tech.metavm.util.Constants;
import tech.metavm.util.Instances;
import tech.metavm.util.InternalException;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.List;

import static java.util.Objects.requireNonNull;

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
        } else {
            throw new ExpressionParsingException();
        }
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
            default -> throw new IllegalStateException("Unexpected prefix: " + expression.prefix.getTokenIndex());
        };
        return new UnaryExpression(operator, parse(expression.expression(0)));
    }

    private ArrayExpression parseArray(MetaVMParser.ListContext list) {
        return new ArrayExpression(
                list.expressionList() != null ?
                        parseExpressionList(list.expressionList()) : List.of(),
                StandardTypes.getAnyArrayType()
        );
    }

    private StaticPropertyExpression parseStaticField(MetaVMParser.ExpressionContext expression) {
        var klass = ((ClassType) parseTypeType(expression.typeType(0))).resolve();
        String identifier = expression.identifier().IDENTIFIER().getText();
        Field field = identifier.startsWith(Constants.CONSTANT_ID_PREFIX) ?
                klass.getField(Id.parse(identifier.substring(Constants.CONSTANT_ID_PREFIX.length()))) :
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

    private Type parseTypeType(MetaVMParser.TypeTypeContext typeType) {
        if (typeType.classOrInterfaceType() != null) {
            var classType = typeType.classOrInterfaceType();
            var identifier = classType.typeIdentifier();
            if (identifier.IDENTIFIER() != null) {
                String name = identifier.IDENTIFIER().getText();
                if (name.startsWith(Constants.CONSTANT_ID_PREFIX)) {
                    return context.getTypeDefProvider().getKlass(
                            Id.parse(name.substring(Constants.CONSTANT_ID_PREFIX.length()))
                    ).getType();
                } else {
                    String className = classType.typeArguments().isEmpty() ? name :
                            name + classType.typeArguments(0).getText();
                    return requireNonNull(context.getTypeDefProvider().findKlassByName(className)).getType();
                }
            }
        }
        throw new ExpressionParsingException();
    }

    private Expression preParseLiteral(MetaVMParser.LiteralContext literal) {
        if (literal.STRING_LITERAL() != null) {
            return parseStringLiteral(literal.STRING_LITERAL());
        } else if (literal.SINGLE_QUOTED_STRING_LITERAL() != null) {
            return parseSingleQuoteLiteral(literal.SINGLE_QUOTED_STRING_LITERAL());
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
        } else {
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

    private Expression parseIdentifier(MetaVMParser.IdentifierContext identifier) {
        if (identifier.IDENTIFIER() != null) {
            String text = identifier.IDENTIFIER().getText();
            if (text.startsWith(Constants.CONSTANT_ID_PREFIX)) {
                return new ConstantExpression(context.getInstanceProvider().get(
                        Id.parse(text.substring(Constants.CONSTANT_ID_PREFIX.length()))));
            } else {
                return new VariableExpression(identifier.IDENTIFIER().getText());
            }
        } else {
            throw new ExpressionParsingException();
        }
    }

}
