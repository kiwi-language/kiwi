package org.metavm.object.instance.search;

import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.metavm.constant.FieldNames;
import org.metavm.expression.*;
import org.metavm.object.instance.core.StringValue;
import org.metavm.object.instance.core.Value;
import org.metavm.object.type.Field;
import org.metavm.object.type.Klass;
import org.metavm.util.Column;
import org.metavm.util.InternalException;
import org.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.metavm.constant.FieldNames.APPLICATION_ID;
import static org.metavm.constant.FieldNames.TYPE;

public class SearchBuilder {

    public static SearchSourceBuilder build(SearchQuery query) {
        SearchSourceBuilder builder = new SearchSourceBuilder();
        builder.from(query.from()).size(query.size());
        builder.query(QueryBuilders.queryStringQuery(buildQueryString(query)));
        builder.sort(FieldNames.ID, SortOrder.DESC);
        return builder;
    }

    public static String buildQueryString(SearchQuery query) {
        String appIdStr = query.appId() > 0 ? query.appId() + "" : "\\" + query.appId();
        String queryString = query.includeBuiltin() ?
                "(" + APPLICATION_ID + ":" + appIdStr + " OR " + APPLICATION_ID + ":\\-1)" :
                "(" + APPLICATION_ID + ":" + appIdStr + ")";
        if (!query.types().isEmpty()) {
            String typeCond = "(" +
                    NncUtils.join(query.types(), t -> TYPE + ":\"" + t + "\"", " OR ") + ")";
            queryString += " AND " + typeCond;
        }
        if (query.condition() != null && !Expressions.isConstantTrue(query.condition())) {
            queryString += " AND " + parse(ExpressionSimplifier.simplify(query.condition()));
        }
        return queryString;
    }

    private static String parse(Expression expression) {
        if (expression instanceof BinaryExpression binaryExpression) {
            return parseBinary(binaryExpression);
        }
        if (expression instanceof UnaryExpression unaryExpression) {
            return parseUnary(unaryExpression);
        }
        if (expression instanceof FunctionExpression functionExpression) {
            return parseFunction(functionExpression);
        }
        if (expression instanceof ConstantExpression constantExpression) {
            return parseConstant(constantExpression);
        }
        throw new RuntimeException("Unsupported expression: " + expression.getClass().getName());
    }

    private static String parseConstant(ConstantExpression expression) {
        return toString(expression.getValue().toSearchConditionValue());
    }

    private static String parseFunction(FunctionExpression expression) {
        var func = expression.getFunction();
        if (func == Func.STARTS_WITH || func == Func.CONTAINS) {
            Expression fieldExpr = expression.getArguments().get(0);
            ConstantExpression constExpr = (ConstantExpression) expression.getArguments().get(1);
            Value constValue = constExpr.getValue();
            SearchField searchField = getColumn(fieldExpr);
            String columnName = func == Func.CONTAINS ? searchField.fuzzyName() : searchField.name();
            String value = func == Func.STARTS_WITH ?
                    escape(((StringValue) constValue).getValue()) + "*" :
                    toString(constValue.toSearchConditionValue());
            return parenthesize(columnName + ":" + value);
        } else {
            throw new InternalException("Unsupported function " + func);
        }
    }

    private static String parseBinary(BinaryExpression expression) {
        BinaryOperator operator = expression.getOperator();

        if (operator == BinaryOperator.EQ
                || operator == BinaryOperator.GT || operator == BinaryOperator.GE
                || operator == BinaryOperator.LT || operator == BinaryOperator.LE
        ) {
            Expression fieldExpr = expression.getVariableChild();
            ConstantExpression constExpr = expression.getConstChild();
            Value constValue = constExpr.getValue();
            SearchField searchField = getColumn(fieldExpr);
            String columnName = searchField.name();
            String value = toString(constValue.toSearchConditionValue());
            return parenthesize(columnName + getEsOperator(operator) + value);
        }
        if (operator == BinaryOperator.IN) {
            Expression fieldExpr = expression.getVariableChild();
            Iterable<Expression> expressions;
            if (expression.getRight() instanceof ArrayExpression) {
                expressions = expression.getArrayChild().getExpressions();
            } else {
                expressions = List.of(expression.getConstChild());
            }
            List<Object> values = new ArrayList<>();
            for (Expression expr : expressions) {
                if (expr instanceof ConstantExpression constExpr) {
                    values.add(parseConstant(constExpr));
                } else {
                    throw new InternalException("Expression '" + expr + "' is not supported for 'in' operator. " +
                            "Only constant values are supported. ");
                }
            }
            return parenthesize(
                    NncUtils.join(
                            values, v -> getColumn(fieldExpr).name() + ":" + toString(v), " OR "
                    )
            );
        }
        String first = parse(expression.getLeft()),
                second = parse(expression.getRight());
        if (operator == BinaryOperator.AND) {
            return parenthesize(first + " AND " + second);
        }
        if (operator == BinaryOperator.OR) {
            return parenthesize(first + " OR " + second);
        }
        throw new RuntimeException("Unsupported operator: " + operator);
    }

    private static SearchField getColumn(Expression expression) {
        if (expression instanceof ThisExpression) {
            return new SearchField(null, Column.ID);
        } else if (expression instanceof PropertyExpression fieldExpression) {
            var field = (Field) fieldExpression.getProperty();
            return new SearchField(field.getDeclaringType(), field.getColumn());
        } else {
            throw new InternalException("Can not get es field for " + expression);
        }
    }

    private static String getEsOperator(BinaryOperator operator) {
        if (operator == BinaryOperator.LT) {
            return ":<";
        }
        if (operator == BinaryOperator.LE) {
            return ":<=";
        }
        if (operator == BinaryOperator.GT) {
            return ":>";
        }
        if (operator == BinaryOperator.GE) {
            return ":>=";
        }
        return ":";
    }

    private static String parseUnary(UnaryExpression expression) {
        var operator = expression.getOperator();
        String operand = parse(expression.getOperand());
        if (operator == UnaryOperator.NOT) {
            return "!" + operand;
        }
        if (operator == UnaryOperator.IS_NULL) {
            return "!" + parenthesize("_exists_:" + operand);
        }
        if (operator == UnaryOperator.IS_NOT_NULL) {
            return "_exists_:" + operand;
        }
        throw new RuntimeException("Unsupported operator: " + operator);
    }

    public static final Set<Character> SPECIAL_CHARS = Set.of(
            '+', '-', '=', '>', '<', '!', '(', ')', '{', '}',
            '[', ']', '^', '\'', '~', '*', '?', ':', '\\', '/', ' '
    );

    public static final Set<Character> SPECIAL_DOUBLE_CHARS = Set.of(
            '|', '&'
    );

    private static String toString(Object value) {
        if (value instanceof String string) {
            return "\"" + escape(string) + "\"";
        }
        return value != null ? escape(value.toString()) : "null";
    }

    private static String escape(String value) {
        if (value == null) {
            return "";
        }
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            Character c = value.charAt(i);
            if (SPECIAL_CHARS.contains(c)) {
                buf.append('\\').append(c);
            } else if (SPECIAL_DOUBLE_CHARS.contains(c) && i < value.length() - 1 && c == value.charAt(i + 1)) {
                buf.append('\\').append(c).append(c);
                i++;
            } else {
                buf.append(c);
            }
        }
        return buf.toString();
    }

    private static String parenthesize(String str) {
        return "(" + str + ")";
    }

    private record SearchField(
            @Nullable Klass type,
            Column column
    ) {

        String name() {
            if (type == null) {
                return column.name();
            } else {
                return "l" + (type.getAncestorClasses().size()-1) + "." + column.name();
            }
        }

        String fuzzyName() {
            if (type == null) {
                return column.fuzzyName();
            } else {
                return "l" + (type.getAncestorClasses().size()-1) + "." + column.fuzzyName();
            }
        }

    }

}
