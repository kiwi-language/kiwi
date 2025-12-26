package org.metavm.expression;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.metavm.entity.StdKlass;
import org.metavm.object.instance.core.IInstanceContext;
import org.metavm.object.type.*;
import org.metavm.util.Instances;
import org.metavm.util.InternalException;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static java.util.Objects.requireNonNull;

@Slf4j
public class ExpressionParser {

    public static Expression parse(@NotNull Klass type, @NotNull String expression, @NotNull IInstanceContext entityContext) {
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

    private final Tokenizer tokenizer;
    private final ParsingContext context;
    private Token token;
    private final StringBuilder buf = new StringBuilder();

    public ExpressionParser(String expression, ParsingContext context) {
        this.context = context;
        tokenizer = new Tokenizer(expression);
        token = tokenizer.next();
    }

    public Expression parse(@Nullable Type assignedType) {
        Expression expression = expression();
        return resolve(expression, assignedType);
    }

    private Expression resolve(Expression expression, @Nullable Type assignedType) {
        return ExpressionResolverV2.resolve(expression, assignedType, context);
    }

    Expression expression() {
        return ternaryExpr();
    }

    private Expression ternaryExpr() {
        var e = orExpr();
        if (is(TokenKind.QUESTION)) {
            next();
            var truePart = orExpr();
            accept(TokenKind.COLON);
            var falsePart = ternaryExpr();
            return ConditionalExpression.create(e, truePart, falsePart);
        } else
            return e;
    }

    private Expression orExpr() {
        var e = andExpr();
        for (;;) {
            if (is(TokenKind.OR)) {
                next();
                e = new BinaryExpression(BinaryOperator.OR, e, andExpr());
            } else {
                return e;
            }
        }
    }

    private Expression andExpr() {
        var e = bitOrExpr();
        for (;;) {
            if (is(TokenKind.AND)) {
                next();
                e = new BinaryExpression(BinaryOperator.AND, e, bitOrExpr());
            } else {
                return e;
            }
        }
    }

    private Expression bitOrExpr() {
        var e = bitXorExpr();
        for (;;) {
            if (is(TokenKind.BIT_OR)) {
                next();
                e = new BinaryExpression(BinaryOperator.BITWISE_OR, e, bitXorExpr());
            } else {
                return e;
            }
        }
    }

    private Expression bitXorExpr() {
        var e = bitAndExpr();
        for (;;) {
            if (is(TokenKind.BIT_XOR)) {
                next();
                e = new BinaryExpression(BinaryOperator.BITWISE_XOR, e, bitAndExpr());
            } else {
                return e;
            }
        }
    }

    private Expression bitAndExpr() {
        var e = eqExpr();
        for (;;) {
            if (is(TokenKind.BIT_AND)) {
                next();
                e = new BinaryExpression(BinaryOperator.BITWISE_AND, e, eqExpr());
            } else {
                return e;
            }
        }
    }

    private Expression eqExpr() {
        var e = relationExpr();
        for (;;) {
            switch (token.kind) {
                case EQ -> {
                    next();
                    e = new BinaryExpression(BinaryOperator.EQ, e, relationExpr());
                }
                case NE -> {
                    next();
                    e = new BinaryExpression(BinaryOperator.NE, e, relationExpr());
                }
                default -> {
                    return e;
                }
            }
        }
    }

    private Expression relationExpr() {
        var e = shiftExpr();
        for (;;) {
            switch (token.kind) {
                case LT -> {
                    next();
                    e = new BinaryExpression(BinaryOperator.LT, e, shiftExpr());
                }
                case GT -> {
                    next();
                    e = new BinaryExpression(BinaryOperator.GT, e, shiftExpr());
                }
                case LE -> {
                    next();
                    e = new BinaryExpression(BinaryOperator.LE, e, shiftExpr());
                }
                case GE -> {
                    next();
                    e = new BinaryExpression(BinaryOperator.GE, e, shiftExpr());
                }
                case INSTANCEOF -> {
                    next();
                    var type = type();
                    e = new InstanceOfExpression(e, type);
                }
                case LIKE -> {
                    next();
                    e = new BinaryExpression(BinaryOperator.LIKE, e, shiftExpr());
                }
                case IN -> {
                    next();
                    e = new BinaryExpression(BinaryOperator.IN, e, shiftExpr());
                }
                default -> {
                    return e;
                }
            }
        }
    }

    private Expression shiftExpr() {
        var e = addExpr();
        for (;;) {
            switch (token.kind) {
                case SHL -> {
                    next();
                    e = new BinaryExpression(BinaryOperator.LEFT_SHIFT, e, addExpr());
                }
                case SHR -> {
                    next();
                    e = new BinaryExpression(BinaryOperator.RIGHT_SHIFT, e, addExpr());
                }
                case USHR -> {
                    next();
                    e = new BinaryExpression(BinaryOperator.UNSIGNED_RIGHT_SHIFT, e, addExpr());
                }
                default -> {
                    return e;
                }
            }
        }
    }

    private Expression addExpr() {
        var e = mulExpr();
        for (;;) {
            switch (token.kind) {
                case ADD -> e = new BinaryExpression(BinaryOperator.ADD, e, mulExpr());
                case MINUS -> e = new BinaryExpression(BinaryOperator.SUB, e, mulExpr());
                default -> {
                    return e;
                }
            }
        }
    }

    private Expression mulExpr() {
        var e = prefixExpr();
        for (;;) {
            switch (token.kind) {
                case MUL -> e = new BinaryExpression(BinaryOperator.MUL, e, prefixExpr());
                case DIV -> e = new BinaryExpression(BinaryOperator.DIV, e, prefixExpr());
                case MOD -> e = new BinaryExpression(BinaryOperator.MOD, e, prefixExpr());
                default -> {
                    return e;
                }
            }
        }
    }

    private Expression prefixExpr() {
        switch (token.kind) {
            case ADD -> {
                next();
                return new UnaryExpression(UnaryOperator.POS, prefixExpr());
            }
            case MINUS -> {
                next();
                return new UnaryExpression(UnaryOperator.NEG, prefixExpr());
            }
            case NOT -> {
                next();
                return new UnaryExpression(UnaryOperator.NOT, prefixExpr());
            }
            default -> {
                return postfixExpr();
            }
        }
    }

    private Expression postfixExpr() {
        var e = atomExpr();
        for (;;) {
            switch (token.kind) {
                case DOT -> {
                    next();
                    var varExpr = new VariableExpression(name());
                    e = new VariablePathExpression(e, varExpr);
                }
                case LBRACKET -> {
                    next();
                    var indexExpr = expression();
                    accept(TokenKind.RBRACKET);
                    e = new ArrayAccessExpression(e, indexExpr);
                }
                case AS -> {
                    next();
                    var typeName = name();
                    e = new AsExpression(e, typeName);
                }
                default -> {
                    return e;
                }
            }
        }
    }

    private Expression atomExpr() {
        switch (token.kind) {
            case TRUE -> {
                next();
                return new ConstantExpression(Instances.booleanInstance(true));
            }
            case FALSE -> {
                next();
                return new ConstantExpression(Instances.booleanInstance(false));
            }
            case NULL -> {
                next();
                return new ConstantExpression(Instances.nullInstance());
            }
            case INT_LITERAL -> {
                var intText = token.text;
                next();
                return new ConstantExpression(
                        Instances.longInstance(Long.parseLong(intText))
                );
            }
            case FLOAT_LITERAL -> {
                var floatText = token.text;
                next();
                return new ConstantExpression(
                        Instances.doubleInstance(Double.parseDouble(floatText))
                );
            }
            case CHAR_LIT -> {
                var charText = token.text;
                next();
                return new ConstantExpression(
                        Instances.charInstance(charText.charAt(0))
                );
            }
            case STRING_LITERAL -> {
                var strText = token.text;
                next();
                return new ConstantExpression(
                        Instances.stringInstance(strText)
                );
            }
            case IDENT -> {
                var name = token.text;
                next();
                if (is(TokenKind.LPAREN)) {
                    next();
                    var func = Func.fromName(name);
                    var args = new ArrayList<Expression>();
                    if (!is(TokenKind.RPAREN)) {
                        do {
                            args.add(expression());
                        } while (skip(TokenKind.COMMA));
                    }
                    accept(TokenKind.RPAREN);
                    return new FunctionExpression(func ,args);
                } else
                    return new VariableExpression(name);
            }
            case LPAREN -> {
                next();
                var e = expression();
                accept(TokenKind.RPAREN);
                return e;
            }
            case LBRACKET -> {
                next();
                var exprs = new ArrayList<Expression>();
                if (!is(TokenKind.RBRACKET)) {
                    do {
                        exprs.add(expression());
                    } while (skip(TokenKind.COMMA));
                }
                accept(TokenKind.RBRACKET);
                return ArrayExpression.create(exprs);
            }
            default -> throw new ExpressionParsingException("Unexpected token: " + token.kind);
        }
    }


    Type type() {
        return unionType();
    }

    Type unionType() {
        var t = intersectionType();
        if (!is(TokenKind.OR))
            return t;
        var alts = new HashSet<Type>();
        alts.add(t);
        do {
            next();
            alts.add(intersectionType());
        } while (is(TokenKind.OR));
        return new UnionType(alts);
    }

    Type intersectionType() {
        var t = arrayType();
        if (!is(TokenKind.AND))
            return t;
        var bounds = new HashSet<Type>();
        bounds.add(t);
        do {
            next();
            bounds.add(arrayType());
        } while (is(TokenKind.AND));
        return new IntersectionType(bounds);
    }

    Type arrayType() {
        var t = atomType();
        if (!is(TokenKind.LBRACKET))
            return t;
        do {
            t = Types.getArrayType(t);
            next();
            accept(TokenKind.RBRACKET);
        } while (is(TokenKind.LBRACKET));
        return t;
    }

    Type atomType() {
        return switch (token.kind) {
            case LBRACKET -> {
                next();
                var lb = type();
                accept(TokenKind.COMMA);
                var ub = type();
                accept(TokenKind.RBRACKET);
                yield new UncertainType(lb, ub);
            }
            case LPAREN -> {
                next();
                if (skip(TokenKind.RPAREN)) {
                    accept(TokenKind.ARROW);
                    yield new FunctionType(List.of(), type());
                }
                var t = type();
                if (is(TokenKind.COMMA)) {
                    next();
                    var paramTypes = new ArrayList<Type>();
                    paramTypes.add(t);
                    do {
                        paramTypes.add(type());
                    } while (skip(TokenKind.COMMA));
                    accept(TokenKind.ARROW);
                    yield new FunctionType(paramTypes, type());
                } else {
                    accept(TokenKind.RPAREN);
                    if (skip(TokenKind.ARROW))
                        yield new FunctionType(List.of(t), type());
                    else
                        yield t;
                }
            }
            case IDENT -> {
                var text = token.text;
                next();
                yield switch (text) {
                    case "byte" -> PrimitiveType.byteType;
                    case "short" -> PrimitiveType.shortType;
                    case "int" -> PrimitiveType.intType;
                    case "long" -> PrimitiveType.longType;
                    case "boolean" -> PrimitiveType.booleanType;
                    case "string" -> StdKlass.string.type();
                    case "char" -> PrimitiveType.charType;
                    case "double" -> PrimitiveType.doubleType;
                    case "float" -> PrimitiveType.floatType;
                    case "void" -> PrimitiveType.voidType;
                    case "time" -> PrimitiveType.timeType;
                    case "password" -> PrimitiveType.passwordType;
                    case "null" -> NullType.instance;
                    case "any" -> AnyType.instance;
                    case "never" -> NeverType.instance;
                    default -> {
                        buf.setLength(0);
                        buf.append(text);
                        while (is(TokenKind.DOT)) {
                            next();
                            buf.append('.').append(accept(TokenKind.IDENT).text);
                        }
                        var name = buf.toString();
                        var klass = (Klass) requireNonNull(context.getTypeDefProvider().findKlassByName(name));
                        if (is(TokenKind.LT)) {
                            next();
                            var typeArgs = new ArrayList<Type>();
                            do {
                                typeArgs.add(type());
                            } while (skip(TokenKind.COMMA));
                            accept(TokenKind.GT);
                            yield KlassType.create(klass, typeArgs);
                        } else
                            yield klass.getType();
                    }
                };
            }
            default -> throw new org.metavm.expression.ExpressionParsingException("Unexpected token: " + token.kind);
        };
    }

    private String name() {
        return accept(TokenKind.IDENT).text;
    }

    private Token accept(TokenKind tk) {
        if (is(tk)) {
            var t = token;
            next();
            return t;
        } else
            throw new ExpressionParsingException("Expected token: " + tk + ", but found: " + token);
    }

    private void next() {
        token = tokenizer.next();
    }

    private boolean skip(TokenKind tk) {
        if (is(tk)) {
            next();
            return true;
        } else
            return false;
    }

    private boolean is(TokenKind tk) {
        return token.kind == tk;
    }


    private static class Tokenizer {
        private final String s;
        private int pos;
        private final StringBuilder buf = new StringBuilder();

        public Tokenizer(String s) {
            this.s = s;
        }

        char current() {
            return s.charAt(pos);
        }

        Token next() {
            if (pos == s.length())
                return new Token(TokenKind.EOF, "");
            return switch (current()) {
                case 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
                        'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
                        'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
                        'N', 'O', 'P',  'Q',  'R',  'S',  'T',  'U',  'V',  'W',  'X',
                        'Y',  'Z', '_' -> identOrKeyword();
                case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> numberLit();
                case ',' -> {
                    pos++;
                    yield new Token(TokenKind.COMMA, ",");
                }
                case '.' -> {
                    pos++;
                    yield new Token(TokenKind.DOT, ".");
                }
                case '&' -> {
                    pos++;
                    if (current() == '&') {
                        pos++;
                        yield new Token(TokenKind.AND, "&&");
                    }
                    yield new Token(TokenKind.BIT_AND, "&");
                }
                case '|' -> {
                    pos++;
                    if (current() == '|') {
                        pos++;
                        yield new Token(TokenKind.OR, "||");
                    }
                    yield new Token(TokenKind.BIT_OR, "|");
                }
                case '~' -> {
                    pos++;
                    yield new Token(TokenKind.BIT_NOT, "~");
                }
                case '^' -> {
                    pos++;
                    yield new Token(TokenKind.BIT_XOR, "^");
                }
                case '(' -> {
                    pos++;
                    yield new Token(TokenKind.LPAREN, "(");
                }
                case ')' -> {
                    pos++;
                    yield new Token(TokenKind.RPAREN, ")");
                }
                case '[' -> {
                    pos++;
                    yield new Token(TokenKind.LBRACKET, "[");
                }
                case ']' -> {
                    pos++;
                    yield new Token(TokenKind.RBRACKET, "]");
                }
                case '<' -> {
                    pos++;
                    if (current() == '=') {
                        pos++;
                        yield new Token(TokenKind.LE, "<=");
                    }
                    yield new Token(TokenKind.LT, "<");
                }
                case '>' -> {
                    pos++;
                    if( current() == '=') {
                        pos++;
                        yield new Token(TokenKind.GE, ">=");
                    }
                    yield new Token(TokenKind.GT, ">");
                }
                case '?' -> {
                    pos++;
                    yield new Token(TokenKind.QUESTION, "?");
                }
                case '+' -> {
                    pos++;
                    yield new Token(TokenKind.ADD, "+");
                }
                case '!' -> {
                    pos++;
                    if (current() == '=') {
                        pos++;
                        yield new Token(TokenKind.NE, "!=");
                    }
                    yield new Token(TokenKind.NOT, "!");
                }
                case '=' -> {
                    pos++;
                    yield new Token(TokenKind.EQ, "=");
                }
                case '-' -> {
                    pos++;
                    if (current() == '>') {
                        next();
                        yield new Token(TokenKind.ARROW, "->");
                    } else
                        yield new Token(TokenKind.MINUS, "-");
                }
                case '"' -> stringLit();
                case '\'' -> charLit();
                case '@' -> {
                    pos++;
                    yield new Token(TokenKind.AT, "@");
                }
                case ' ', '\t' -> {
                    skipWhitespaces();
                    yield next();
                }
                default -> {
                    if (Character.isJavaIdentifierPart(current()))
                        yield identOrKeyword();
                    else
                        throw new ExpressionParsingException("Unexpected character: " + current());
                }
            };
        }

        private void skipWhitespaces() {
            while (!isEof() && Character.isWhitespace(current()))
                pos++;
        }

        private Token identOrKeyword() {
            buf.setLength(0);
            while (!isEof() && Character.isJavaIdentifierPart(current())) {
                buf.append(current());
                pos++;
            }
            var text = buf.toString();
            var tk = switch (text) {
                case "as" -> TokenKind.AS;
                case "true" -> TokenKind.TRUE;
                case "false" -> TokenKind.FALSE;
                case "like" -> TokenKind.LIKE;
                case "null" -> TokenKind.NULL;
                case "in" -> TokenKind.IN;
                case "instanceof" -> TokenKind.INSTANCEOF;
                default -> TokenKind.IDENT;
            };
            return new Token(tk, text);
        }

        private boolean isEof() {
            return pos >= s.length();
        }

        private Token charLit() {
            accept('\'');
            buf.append(0);
            var c = current();
            pos++;
            if (c == '\\') {
                var c1 = current();
                pos++;
                c = switch (c1) {
                    case '\\' -> '\\';
                    case 'b' -> '\b';
                    case 'f' -> '\f';
                    case 'n' -> '\n';
                    case 'r' -> '\r';
                    case 't' -> '\t';
                    case '\'' -> '\'';
                    case 'u' -> {
                        var code = 0;
                        for (int i = 0; i < 4; i++) {
                            code = code << 4 | hex(current());
                            pos++;
                        }
                        yield (char) code;
                    }
                    default -> throw new org.metavm.expression.ExpressionParsingException("Invalid escape character: " + c1);
                };
            }
            accept('\'');
            return new Token(TokenKind.CHAR_LIT, Character.toString(c));
        }

        private Token stringLit() {
            accept('"');
            buf.setLength(0);
            for (;;) {
                var c = current();
                pos++;
                switch (c) {
                    case '"' -> {
                        return new Token(TokenKind.STRING_LITERAL, buf.toString());
                    }
                    case '\\' -> {
                        var c1 = current();
                        pos++;
                        switch (c1) {
                           case '"' -> buf.append('"');
                           case 'b' -> buf.append('\b');
                           case 'f' -> buf.append('\f');
                           case 'n' -> buf.append('\n');
                           case 'r' -> buf.append('\r');
                           case 't' -> buf.append('\t');
                           case '\\' -> buf.append('\\');
                           case 'u' -> {
                               var code = 0;
                               for (int i = 0; i < 4; i++) {
                                  code = code << 4 | hex(current());
                                  next();
                               }
                               buf.appendCodePoint(code);
                           }
                        }
                    }
                    default -> buf.append(c);
                }
            }
        }

        private Token numberLit() {
            buf.setLength(0);
            if (current() == '-') {
                buf.append('-');
                pos++;
            }
            scanDigits();
            var tk = TokenKind.INT_LITERAL;
            if (current() == '.') {
                pos++;
                scanDigits();
                tk = TokenKind.FLOAT_LITERAL;
            }
            if (current() == 'e' || current() == 'E') {
                buf.append('e');
                tk = TokenKind.FLOAT_LITERAL;
                if (current() == '-') {
                    buf.append('-');
                    pos++;
                }
                scanDigits();
            }
            return new Token(tk, buf.toString());
        }

        private void scanDigits() {
            if (!isDigit())
                throw new ExpressionParsingException("Digit expected, but found: " + current());
            do {
                buf.append(current());
                pos++;
            } while (isDigit());
        }

        private boolean isDigit() {
            var c = current();
            return c >= '0' && c <= '9';
        }

        private int hex(char c) {
            if (c >= '0' && c <= '9')
                return c - '0';
            if (c >= 'a' && c <= 'f')
                return 10 + (c - 'a');
            if (c >= 'A' && c <= 'F')
                return 10 + (c - 'A');
            throw new ExpressionParsingException("Invalid hex character: " + c);
        }

        private void accept(char c) {
            if (current() == c)
                pos++;
            else
                throw new ExpressionParsingException("Expected character: " + c + ", but found: " + current());
        }

    }

    private record Token(TokenKind kind, String text) {}

    private enum TokenKind {
        IDENT, DOT, COMMA, COLON, ARROW, LPAREN, RPAREN, LBRACKET, RBRACKET, EOF, BIT_AND, BIT_OR, BIT_XOR, LT, GT, ADD, MINUS,
        MUL, DIV, MOD, NOT, QUESTION, AS, DOUBLE_QUOTE, QUOTE,
        INT_LITERAL, FLOAT_LITERAL, STRING_LITERAL, TRUE, FALSE, INSTANCEOF, BIT_NOT, SHL, SHR, USHR, EQ, NE,
        GE, LE, AND, OR, LIKE, AT, CHAR_LIT, IN, NULL
    }


    public static class ExpressionParsingException extends RuntimeException {

        public ExpressionParsingException(String message) {
            super(message);
        }
    }




}
