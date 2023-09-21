package tech.metavm.expression;

import tech.metavm.entity.IInstanceContext;
import tech.metavm.object.instance.PrimitiveInstance;
import tech.metavm.object.meta.ClassType;
import tech.metavm.util.Constants;
import tech.metavm.util.InstanceUtils;
import tech.metavm.util.InternalException;
import tech.metavm.util.NncUtils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class ExpressionParser {

    public static final int FUNC_PRECEDENCE = 0;

    public static final int OPEN_BRACKET_PRECEDENCE = 0;

    public static Expression parse(ClassType type, String expression, IInstanceContext instanceContext) {
        return parse(expression, new TypeParsingContext(type, instanceContext));
    }

    public static Expression parse(String expression, ParsingContext context) {
        return parse(expression, 0, context);
    }

    public static Expression parse(String expression, int offset, ParsingContext context) {
        if(NncUtils.isEmpty(expression)) {
            return null;
        }
        return new ExpressionParser(expression, offset, context).parse();
    }

    private final ExpressionTokenizer tokenizer;
    private final LinkedList<Expression> exprStack = new LinkedList<>();
    private final LinkedList<Op> opStack = new LinkedList<>();
    private final ParsingContext context;
    private int numUnpairedLeftParentheses = 0;

    public ExpressionParser(String expression, int offset, ParsingContext context) {
        this(new ExpressionTokenizer(expression, offset), context);
    }

    ExpressionParser(ExpressionTokenizer tokenizer, ParsingContext context) {
        this.tokenizer = tokenizer;
        this.context = context;
    }

    public Expression parse() {
        Expression expression = preParse();
        return resolve(expression);
    }

    private Expression resolve(Expression expression) {
        return ExpressionResolver.resolve(expression, context);
    }

    private Expression preParse() {
        while (tokenizer.hasNext() &&
                !(tokenizer.peekToken().isClosingParenthesis() && numUnpairedLeftParentheses == 0)) {
            Token token = tokenizer.nextToken();
            if(token.isOpenParenthesis()) {
                numUnpairedLeftParentheses++;
                opStack.push(Op.op(Operator.OPEN_PARENTHESIS));
            }
            else if(token.isClosingBracket()) {
                while (!opStack.isEmpty() && !Op.isOpenBracket(opStack.peek())) {
                    writeExpression();
                }
                if(opStack.isEmpty()) {
                    throw new RuntimeException("Unpaired closing bracket");
                }
                opStack.pop();
                if(exprStack.size() < 2) {
                    throw new RuntimeException("Invalid array access expression");
                }
                var index = exprStack.pop();
                var array = exprStack.pop();
                exprStack.push(new ArrayAccessExpression(array, index));
            }
            else if(token.isClosingParenthesis()) {
                numUnpairedLeftParentheses--;
                while (!opStack.isEmpty() && !Op.isOpenParenthesis(opStack.peek())) {
                    writeExpression();
                }
                if(opStack.isEmpty()) {
                    throw new RuntimeException("Unpaired closing parenthesis");
                }
                opStack.pop();
            }
            else if(token.isOperator()) {
                Operator operator = token.getOperator();
                while (hasPrecedentOp(operator.precedence())) {
                    writeExpression();
                }
                opStack.push(Op.op(operator));
            }
            else if(token.isFunction()) {
                while (hasPrecedentOp(FUNC_PRECEDENCE)) {
                    writeExpression();
                }
                opStack.push(Op.func(token.getFunction()));
            }
            else if(token.isOpenBracket()) {
                while (hasPrecedentOp(OPEN_BRACKET_PRECEDENCE)) {
                    writeExpression();
                }
                opStack.push(Op.op(Operator.OPEN_BRACKET));
            }
            else if(token.isAllMatch()) {
                opStack.push(new AllMatchOp());
//                tokenizer.nextToken(TokenType.LEFT_PARENTHESIS);
//                Expression arrayExpr = parseConstantOrField(tokenizer.nextToken());
//                CursorExpression cursor = new CursorExpression(arrayExpr, parseAlias());
//                tokenizer.nextOperator(Operator.COMMA);
//                Expression subExpr = parseSubExpression(cursor);
//                tokenizer.nextToken(TokenType.RIGHT_PARENTHESIS);
//                exprStack.push(new AllMatchExpression(cursor, subExpr));
            }
            else if(token.isAs()) {
                Token alias = tokenizer.nextVariable();
                exprStack.push(new AsExpression(exprStack.pop(), alias.rawValue()));
            }
            else {
                exprStack.push(parseConstantOrField(token));
            }
        }

        while (!opStack.isEmpty()) {
            writeExpression();
        }

        if(exprStack.size() != 1) {
            throw new RuntimeException("Expression stack must have one element at this point");
        }
        return exprStack.pop();
    }

    private QueryStringException incorrectTokenType(@SuppressWarnings("SameParameterValue")
                                                    TokenType expectedTokenType, Token actualToken) {
        return new QueryStringException("Expecting token type " + expectedTokenType.name() + " at position "
                +  tokenizer.getPosition() + " but got " + actualToken);
    }

    private String parseAlias() {
        if(tokenizer.peekToken().isAs()) {
            tokenizer.nextKeyword(Token.AS);
            return tokenizer.nextVariable().rawValue();
        }
        else if(tokenizer.peekToken().isVariable()) {
            return tokenizer.nextVariable().rawValue();
        }
        else {
            return null;
        }
    }

    private Expression parseSubExpression(CursorExpression cursor) {
        SubParsingContext subParsingContext = new SubParsingContext(cursor, context);
        return new ExpressionParser(tokenizer, subParsingContext).parse();
    }

    private Expression parseConstantOrField(Token token) {
        if(token.isConstant()) {
            PrimitiveInstance constValue;
            if (token.isString()) {
                constValue = InstanceUtils.createString((String) token.value());
            }
            else if (token.isInt()) {
                constValue = InstanceUtils.createLong((long) token.value());
            }
            else if(token.isFloat()) {
                constValue = InstanceUtils.createDouble((double) token.value());
            }
            else if (token.isBoolean()) {
                constValue = InstanceUtils.createBoolean((boolean) token.value());
            }
            else if (token.isNull()) {
                constValue = InstanceUtils.createNull();
            }
            else {
                throw new InternalException("Invalid constant token type " + token.type());
            }
            return new ConstantExpression(constValue);
        }
        else if(token.isIdConstant()) {
            long id = Long.parseLong(token.rawValue().substring(Constants.CONSTANT_ID_PREFIX.length()));
            return new ConstantExpression(context.getInstance(id));
        }
        else if(token.isVariable()){
            return parseVariable(token.getName());
        }
        else {
            throw incorrectTokenType(TokenType.VARIABLE, token);
        }
    }

    private VariableExpression parseVariable(String variable) {
        return new VariableExpression(variable);
    }

    private Expression parseField(String fieldPath) {
        if(fieldPath.startsWith(Constants.CONSTANT_ID_PREFIX)) {
            try {
                long id = Long.parseLong(fieldPath.substring(Constants.CONSTANT_ID_PREFIX.length()));
                return new ConstantExpression(context.getInstance(id));
            }
            catch (NumberFormatException e) {
                throw new InternalException("Invalid id constant '" + fieldPath + "'");
            }
        }
        else {
            String[] splits = fieldPath.split("\\.");
            List<Var> vars = new ArrayList<>();
            for (String split : splits) {
                if (split.startsWith("$")) {
                    vars.add(new Var(VarType.ID, Long.parseLong(split.substring(1))));
                } else {
                    vars.add(new Var(VarType.NAME, split));
                }
            }
            return context.parse(vars);
        }
    }

    private boolean hasPrecedentOp(int precedence) {
        return !opStack.isEmpty() && opStack.peek().precedence() <= precedence;
    }

    private void writeExpression() {
        Op op = opStack.pop();
        if(op instanceof FunctionOp funcOp) {
            FunctionExpression funcExpr;
            if(funcOp.func.getParameterTypes().isEmpty()) {
                funcExpr = new FunctionExpression(funcOp.func);
            }
            else {
                funcExpr = new FunctionExpression(funcOp.func, exprStack.pop());
            }
            exprStack.push(funcExpr);
        }
        else if(op instanceof OperatorOp operatorOp) {
            if(operatorOp.isDot()) {
                Expression field = popExpr();
                Expression qualifier = popExpr();
                if(field instanceof VariableExpression fieldVariableExpr) {
                    exprStack.push(new VariablePathExpression(qualifier, fieldVariableExpr));
                }
                else {
                    throw new InternalException("Invalid field expression. " +
                            "Expecting a field name, but got " + field);
                }
            }
            else if (operatorOp.isComma()) {
                Expression second = popExpr(), first = popExpr();
                exprStack.push(ArrayExpression.merge(first, second));
            } else if (operatorOp.isUnary()) {
                Expression expression = popExpr();
                exprStack.push(new UnaryExpression(operatorOp.op(), expression));
            } else {
                Expression second = popExpr(), first = popExpr();
                exprStack.push(new BinaryExpression(operatorOp.op(), first, second));
            }
        }
        else if(op instanceof AllMatchOp) {
            Expression top = exprStack.pop();
            if(top instanceof ArrayExpression args) {
                if(args.getExpressions().size() != 2) {
                    throw new InternalException(
                            "Expecting 2 elements in array expression, but got "
                                    + args.getExpressions().size()
                    );
                }
                Expression array = args.getExpressions().get(0);
                Expression condition = args.getExpressions().get(1);
                exprStack.push(new AllMatchExpression(array, condition, null));
            }
            else {
                throw new InternalException("Expecting array expression for AllMatch, but got " + top);
            }
        }
        else {
            throw new InternalException("Unrecognized op " + op);
        }
    }

    public Expression popExpr() {
        if(exprStack.isEmpty()) {
            throw new QueryStringException("Expression stack underflow, expression: " + tokenizer.getExpression());
        }
        return exprStack.pop();
    }

    private interface Op {

        static Op op(Operator operator) {
            return new OperatorOp(operator);
        }

        static Op func(Function function) {
            return new FunctionOp(function);
        }

        static boolean isOpenParenthesis(Op op) {
            return op instanceof OperatorOp oop && oop.isOpenParenthesis();
        }

        static boolean isOpenBracket(Op op) {
            return op instanceof OperatorOp oop && oop.isOpenBracket();
        }

        default boolean isFunc() {
            return this instanceof OperatorOp;
        }

        default boolean isOp() {
            return this instanceof OperatorOp;
        }

        int precedence();

    }

    private record OperatorOp
    (
        Operator op
    ) implements Op {

        boolean isOpenParenthesis() {
            return op == Operator.OPEN_PARENTHESIS;
        }

        boolean isOpenBracket() {
            return op == Operator.OPEN_BRACKET;
        }

        boolean isComma() {
            return op == Operator.COMMA;
        }

        boolean isDot() {
            return op == Operator.DOT;
        }

        boolean isUnary() {
            return op.isUnary();
        }

        public int precedence() {
            return op.precedence();
        }
    }

    private record FunctionOp(
            Function func
    ) implements Op
    {

        @Override
        public int precedence() {
            return FUNC_PRECEDENCE;
        }
    }

    private static class AllMatchOp implements Op {

        @Override
        public int precedence() {
            return FUNC_PRECEDENCE;
        }
    }

}
