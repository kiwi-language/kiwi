package tech.metavm.object.instance.query;

import tech.metavm.object.meta.Type;
import tech.metavm.util.NncUtils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class ExpressionParser {

    public static final int FUNC_PRECEDENCE = 0;

    public static Expression parse(Type type, String expression) {
        return parse(expression, new TypeParsingContext(type));
    }

    public static Expression parse(String expression, ParsingContext context) {
        if(NncUtils.isEmpty(expression)) {
            return null;
        }
        return new ExpressionParser(expression, context).parse();
    }

    private final ExpressionTokenizer tokenizer;
    private final LinkedList<Expression> exprStack = new LinkedList<>();
    private final LinkedList<Op> opStack = new LinkedList<>();
    private final ParsingContext context;

    public ExpressionParser(String expression, ParsingContext context) {
        this.tokenizer = new ExpressionTokenizer(expression);
        this.context = context;
    }

    public Expression parse() {
        while (tokenizer.hasNext()) {
            Token token = tokenizer.next();

            if(token.isLeftParenthesis()) {
                opStack.push(Op.op(Operator.LEFT_PARENTHESIS));
            }
            else if(token.isRightParenthesis()) {
                while (!opStack.isEmpty() && !opStack.peek().isLeftParenthesis()) {
                    writeExpression();
                }
                if(opStack.isEmpty()) {
                    throw new RuntimeException("Unpaired right parenthesis");
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
            else {
                if(token.isConstant()) {
                    exprStack.push(new ConstantExpression(token.value(), context.getEntityContext()));
                }
                else {
                    exprStack.push(parseField(token.getName()));
                }
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

    private Expression parseField(String fieldPath) {
        String[] splits = fieldPath.split("\\.");
        List<Var> vars = new ArrayList<>();
        for (String split : splits) {
            if(split.startsWith("$")) {
                vars.add(new Var(VarType.ID, Long.parseLong(split.substring(1))));
            }
            else {
                vars.add(new Var(VarType.NAME, split));
            }
        }
        return context.parse(vars);
    }

    private boolean hasPrecedentOp(int precedence) {
        return !opStack.isEmpty() && opStack.peek().precedence() <= precedence;
    }

    private void writeExpression() {
        Op op = opStack.pop();
        if(op.isFunc()) {
            Expression arg = popExprRequired();
            exprStack.push(new FunctionExpression(op.func(), arg, context.getEntityContext()));
        }
        else if(op.isComma()) {
            Expression second = popExprRequired(), first = popExprRequired();
            exprStack.push(ArrayExpression.merge(first, second));
        }
        else if(op.isUnary()) {
            Expression expression = popExprRequired();
            exprStack.push(new UnaryExpression(op.op(), expression));
        }
        else {
            Expression second = popExprRequired(), first = popExprRequired();
            exprStack.push(new BinaryExpression(op.op(), first, second));
        }
    }

    public Expression popExprRequired() {
        if(exprStack.isEmpty()) {
            throw new QueryStringException("Expression stack underflow");
        }
        return exprStack.pop();
    }

    private static record Op (
        Operator op,
        Function func
    ) {

        boolean isFunc() {
            return func != null;
        }

        boolean isOp() {
            return op != null;
        }

        boolean isLeftParenthesis() {
            return op == Operator.LEFT_PARENTHESIS;
        }

        boolean isComma() {
            return op == Operator.COMMA;
        }

        boolean isUnary() {
            return isOp() && op.isUnary();
        }

        int precedence() {
            return isOp() ? op.precedence() : FUNC_PRECEDENCE;
        }

        static Op op(Operator operator) {
            return new Op(operator, null);
        }

        static Op func(Function function) {
            return new Op(null, function);
        }

    }

    public static void main(String[] args) {
        String query = "material.creator.name starts with '钢' and max(amount, aux_amount) > 100";
        Expression expression = ExpressionParser.parse(null, query);
        System.out.println(expression);
    }

}
