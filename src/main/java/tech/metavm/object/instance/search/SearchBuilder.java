package tech.metavm.object.instance.search;

import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import tech.metavm.constant.FieldNames;
import tech.metavm.object.instance.Instance;
import tech.metavm.object.instance.StringInstance;
import tech.metavm.object.instance.query.*;
import tech.metavm.util.Column;
import tech.metavm.util.InternalException;
import tech.metavm.util.NncUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static tech.metavm.constant.FieldNames.TENANT_ID;
import static tech.metavm.constant.FieldNames.TYPE_ID;

public class SearchBuilder {

    public static SearchSourceBuilder build(SearchQuery query) {
        SearchSourceBuilder builder = new SearchSourceBuilder();
        builder.from(query.from()).size(query.size());
        builder.query(QueryBuilders.queryStringQuery(buildQueryString(query)));
        builder.sort(FieldNames.ID, SortOrder.DESC);
        return builder;
    }

    public static String buildQueryString(SearchQuery query) {
        String tenantIdStr = query.tenantId() > 0 ? query.tenantId() + "" : "\\" + query.tenantId();
        String queryString = "(" + TENANT_ID +  ":" + tenantIdStr + /*" OR " + TENANT_ID +  ":\\-1*/ ")";
        if(!query.typeIds().isEmpty()) {
            String typeIdCondition = "(" +
                    NncUtils.join(query.typeIds(), id -> TYPE_ID + ":" + id, " OR ") + ")";
            queryString += " AND " + typeIdCondition;
        }
        if(query.condition() != null && !ExpressionUtil.isConstantTrue(query.condition())) {
            queryString += " AND " + parse(query.condition());
        }
        return queryString;
    }

    private static String parse(Expression expression) {
        if(expression instanceof BinaryExpression binaryExpression) {
            return parseBinary(binaryExpression);
        }
        if(expression instanceof UnaryExpression unaryExpression) {
            return parseUnary(unaryExpression);
        }
        if(expression instanceof ConstantExpression constantExpression) {
            return parseConstant(constantExpression);
        }
        throw new RuntimeException("Unsupported expression category: " + expression.getClass().getName());
    }

    private static String parseConstant(ConstantExpression expression) {
        return toString(expression.getValue().toSearchConditionValue());
    }

    private static String parseBinary(BinaryExpression expression) {
        Operator operator = expression.getOperator();

        if(operator == Operator.EQ || operator == Operator.LIKE || operator == Operator.STARTS_WITH
                || operator == Operator.GT || operator == Operator.GE
                || operator == Operator.LT || operator == Operator.LE
        ) {
            Expression fieldExpr = expression.getVariableChild();
            ConstantExpression constExpr = expression.getConstChild();
            Instance constValue = constExpr.getValue();
            Column column = getColumn(fieldExpr);
            String columnName = operator == Operator.LIKE ? column.fuzzyName() : column.name();
            String value = operator == Operator.STARTS_WITH ?
                    escape(((StringInstance) constValue).getValue()) + "*" :
                    toString(constValue.toSearchConditionValue());
            return parenthesize(columnName + getEsOperator(operator) + value);
        }
        if(operator == Operator.IN) {
            Expression fieldExpr = expression.getVariableChild();
            ArrayExpression arrayExpr;
            if(expression.getSecond() instanceof ArrayExpression) {
                arrayExpr = expression.getArrayChild();
            }
            else {
                arrayExpr = new ArrayExpression(List.of(expression.getConstChild()));
            }
            List<Object> values = new ArrayList<>();
            for (Expression expr : arrayExpr.getExpressions()) {
                if(expr instanceof ConstantExpression constExpr) {
                    values.add(parseConstant(constExpr));
                }
                else {
                    throw new InternalException("Expression '" + expr + "' is not supported for IN operator. " +
                            "Only constant values are supported. ");
                }
            }
            return parenthesize(
                    NncUtils.join(
                        values, v -> getColumn(fieldExpr).name() + ":" + toString(v), " OR "
                    )
                );
        }
        String first = parse(expression.getFirst()),
                second = parse(expression.getSecond());
        if(operator == Operator.AND) {
            return parenthesize(first + " AND " +second);
        }
        if(operator == Operator.OR) {
            return parenthesize(first + " OR " +second);
        }
        throw new RuntimeException("Unsupported operator: " + operator);
    }

    private static Column getColumn(Expression expression) {
        if(expression instanceof ThisExpression) {
            return Column.ID;
        }
        else if(expression instanceof FieldExpression fieldExpression) {
            return fieldExpression.getLastField().getColumn();
        }
        else {
            throw new InternalException("Can not get es field for " + expression);
        }
    }

    private static String getEsOperator(Operator operator) {
        if(operator == Operator.LT) {
            return ":<";
        }
        if(operator == Operator.LE) {
            return ":<=";
        }
        if(operator == Operator.GT) {
            return ":>";
        }
        if(operator == Operator.GE) {
            return ":>=";
        }
        return ":";
    }

    private static String parseUnary(UnaryExpression expression) {
        Operator operator = expression.getOperator();
        String operand = parse(expression.getOperand());
        if(operator == Operator.NOT) {
            return "!" + operand;
        }
        if(operator == Operator.IS_NULL) {
            return "!" + parenthesize("_exists_:" + operand);
        }
        if(operator == Operator.IS_NOT_NULL) {
            return "_exists_:" + operand;
        }
        throw new RuntimeException("Unsupported operator: " + operator);
    }

    public static final Set<Character> SPECIAL_CHARS = Set.of(
            '+', '-', '=',  '>', '<', '!', '(' ,')' ,'{' ,'}',
            '[' ,']', '^' ,'\'', '~', '*', '?', ':', '\\', '/', ' '
    );

    public static final Set<Character> SPECIAL_DOUBLE_CHARS = Set.of(
            '|', '&'
    );

    private static String toString(Object value) {
        if(value instanceof String string) {
            return "\"" + escape(string) + "\"";
        }
        return value != null ? escape(value.toString()) : "null";
    }

    private static String escape(String value) {
        if(value == null) {
            return "";
        }
        StringBuilder buf = new StringBuilder();
        for(int i = 0; i < value.length(); i++) {
            Character c = value.charAt(i);
            if(SPECIAL_CHARS.contains(c)) {
                buf.append('\\').append(c);
            }
            else if(SPECIAL_DOUBLE_CHARS.contains(c) && i < value.length() - 1 && c == value.charAt(i + 1)) {
                buf.append('\\').append(c).append(c);
                i++;
            }
            else {
                buf.append(c);
            }
        }
        return buf.toString();
    }

    private static String parenthesize(String str) {
        return "(" + str + ")";
    }

}
