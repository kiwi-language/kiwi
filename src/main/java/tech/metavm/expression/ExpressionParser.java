package tech.metavm.expression;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.TerminalNode;
import tech.metavm.entity.IEntityContext;
import tech.metavm.expression.antlr.InstacodeLexer;
import tech.metavm.expression.antlr.InstacodeParser;
import tech.metavm.object.instance.core.IInstanceContext;
import tech.metavm.object.instance.query.OperatorTypes;
import tech.metavm.object.type.ClassType;
import tech.metavm.object.type.Field;
import tech.metavm.object.type.StandardTypes;
import tech.metavm.object.type.Type;
import tech.metavm.util.Constants;
import tech.metavm.util.InstanceUtils;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.List;

import static java.util.Objects.requireNonNull;

public class ExpressionParser {

    public static Expression parse(ClassType type, String expression, IInstanceContext instanceContext) {
        return parse(expression, new TypeParsingContext(type, instanceContext));
    }

    public static Expression parse(String expression, ParsingContext context) {
        return parse(expression, null , context);
    }

    public static Expression parse(String expression, @Nullable Type assignedType, ParsingContext context) {
        return new ExpressionParser(expression, context).parse(assignedType);
    }

    private final InstacodeParser parser;
    private final ParsingContext context;

    public ExpressionParser(String expression, ParsingContext context) {
        this.context = context;
        CharStream input = CharStreams.fromString(expression);
        parser = new InstacodeParser(new CommonTokenStream(new InstacodeLexer(input)));
    }

    public Expression parse(@Nullable Type assignedType) {
        Expression expression = antlrPreparse();
        return resolve(expression, assignedType);
    }

    private Expression resolve(Expression expression, @Nullable Type assignedType) {
        return ExpressionResolver.resolve(expression, assignedType, context);
    }

    Expression antlrPreparse() {
        return parse(parser.expression());
    }

    private Expression parse(InstacodeParser.ExpressionContext expression) {
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

    private Expression parseAs(InstacodeParser.ExpressionContext expression) {
        return new AsExpression(
                parse(expression.expression(0)),
                expression.IDENTIFIER().getText()
        );
    }

    private Expression parseAllMatch(InstacodeParser.AllMatchContext allMatch) {
        return new AllMatchExpression(
                parse(allMatch.expression(0)),
                parse(allMatch.expression(1))
        );
    }

    private Expression parseMethodCall(InstacodeParser.MethodCallContext methodCall) {
        if (methodCall.identifier().IDENTIFIER() != null) {
            return new FunctionExpression(
                    Function.getByName(methodCall.identifier().IDENTIFIER().getText()),
                    methodCall.expressionList() != null ?
                            parseExpressionList(methodCall.expressionList())
                            : List.of()
            );
        } else {
            throw new ExpressionParsingException();
        }
    }

    private List<Expression> parseExpressionList(InstacodeParser.ExpressionListContext expressionList) {
        return NncUtils.map(expressionList.expression(), this::parse);
    }

    private Expression parsePrefix(InstacodeParser.ExpressionContext expression) {
        return switch (expression.prefix.getType()) {
            case InstacodeParser.ADD -> new UnaryExpression(
                    Operator.POS, parse(expression.expression(0))
            );
            case InstacodeParser.SUB -> new UnaryExpression(
                    Operator.NEG, parse(expression.expression(0))
            );
            default -> throw new IllegalStateException("Unexpected prefix: " + expression.prefix.getTokenIndex());
        };
    }

    private ArrayExpression parseArray(InstacodeParser.ListContext list) {
        return new ArrayExpression(
                list.expressionList() != null ?
                        parseExpressionList(list.expressionList()) : List.of(),
                StandardTypes.getObjectArrayType()
        );
    }

    private StaticFieldExpression parseStaticField(InstacodeParser.ExpressionContext expression) {
        var type = (ClassType) parseTypeType(expression.typeType(0));
        String identifier = expression.identifier().IDENTIFIER().getText();
        Field field = identifier.startsWith(Constants.CONSTANT_ID_PREFIX) ?
                type.getField(Long.parseLong(identifier.substring(Constants.CONSTANT_ID_PREFIX.length()))) :
                type.getFieldByName(identifier);
        return new StaticFieldExpression(field);
    }

    private Expression parseArrayAccess(InstacodeParser.ExpressionContext expression) {
        return new ArrayAccessExpression(
                parse(expression.expression(0)),
                parse(expression.expression(1))
        );
    }

    private Expression preParsePrimary(InstacodeParser.PrimaryContext primary) {
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

    private Expression parseBop(InstacodeParser.ExpressionContext expression) {
        var bop = expression.bop;
        return switch (bop.getType()) {
            case InstacodeParser.DOT -> {
                if (expression.identifier() != null) {
                    yield new VariablePathExpression(
                            parse(expression.expression(0)),
                            (VariableExpression) parseIdentifier(expression.identifier())
                    );
                } else {
                    throw new ExpressionParsingException();
                }
            }
            case InstacodeParser.INSTANCEOF -> new InstanceOfExpression(
                    parse(expression.expression(0)),
                    parseTypeType(expression.typeType(0))
            );
            case InstacodeParser.QUESTION -> new ConditionalExpression(
                    parse(expression.expression(0)),
                    parse(expression.expression(1)),
                    parse(expression.expression(2))
            );
            default -> new BinaryExpression(
                    Operator.getByOp(bop.getText(), OperatorTypes.BINARY),
                    parse(expression.expression(0)),
                    parse(expression.expression(1))
            );
        };
    }

    private Type parseTypeType(InstacodeParser.TypeTypeContext typeType) {
        if (typeType.classOrInterfaceType() != null) {
            var classType = typeType.classOrInterfaceType();
            var identifier = classType.typeIdentifier();
            if (identifier.IDENTIFIER() != null) {
                String name = identifier.IDENTIFIER().getText();
                if (name.startsWith(Constants.CONSTANT_ID_PREFIX)) {
                    return getEntityContext().getType(
                            Long.parseLong(name.substring(Constants.CONSTANT_ID_PREFIX.length()))
                    );
                } else {
                    String className = classType.typeArguments().isEmpty() ? name :
                            name + classType.typeArguments(0).getText();
                    return requireNonNull(context.getEntityContext()).selectByUniqueKey(
                            ClassType.UNIQUE_NAME, className
                    );
                }
            }
        }
        throw new ExpressionParsingException();
    }

    private Expression preParseLiteral(InstacodeParser.LiteralContext literal) {
        if (literal.STRING_LITERAL() != null) {
            return parseStringLiteral(literal.STRING_LITERAL());
        } else if (literal.SINGLE_QUOTED_STRING_LITERAL() != null) {
            return parseSingleQuoteLiteral(literal.SINGLE_QUOTED_STRING_LITERAL());
        } else if (literal.integerLiteral() != null) {
            String intText = literal.integerLiteral().getText();
            return new ConstantExpression(
                    InstanceUtils.longInstance(Long.parseLong(intText))
            );
        } else if (literal.floatLiteral() != null) {
            return new ConstantExpression(
                    InstanceUtils.doubleInstance(Double.parseDouble(literal.getText()))
            );
        } else if (literal.BOOL_LITERAL() != null) {
            return new ConstantExpression(
                    InstanceUtils.booleanInstance(Boolean.parseBoolean(literal.getText()))
            );
        } else if (literal.NULL_LITERAL() != null) {
            return new ConstantExpression(InstanceUtils.nullInstance());
        } else {
            throw new ExpressionParsingException();
        }
    }

    private Expression parseStringLiteral(TerminalNode stringLiteral) {
        return new ConstantExpression(
                InstanceUtils.stringInstance(NncUtils.deEscapeDoubleQuoted(stringLiteral.getText()))
        );
    }

    private Expression parseSingleQuoteLiteral(TerminalNode singleQuoteStringLiteral) {
        return new ConstantExpression(
                InstanceUtils.stringInstance(NncUtils.deEscapeSingleQuoted(singleQuoteStringLiteral.getText()))
        );
    }

    private Expression parseIdentifier(InstacodeParser.IdentifierContext identifier) {
        if (identifier.IDENTIFIER() != null) {
            String text = identifier.IDENTIFIER().getText();
            if (text.startsWith(Constants.CONSTANT_ID_PREFIX)) {
                return new ConstantExpression(requireNonNull(context.getInstanceContext()).get(
                        Long.parseLong(text.substring(Constants.CONSTANT_ID_PREFIX.length()))));
            } else {
                return new VariableExpression(identifier.IDENTIFIER().getText());
            }
        } else {
            throw new ExpressionParsingException();
        }
    }

    private IEntityContext getEntityContext() {
        return requireNonNull(context.getInstanceContext()).getEntityContext();
    }

}
