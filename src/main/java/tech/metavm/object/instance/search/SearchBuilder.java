package tech.metavm.object.instance.search;

import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import tech.metavm.object.instance.query.*;
import tech.metavm.util.Column;
import tech.metavm.util.NncUtils;

import java.util.List;
import java.util.Set;

import static tech.metavm.constant.FieldNames.TENANT_ID;
import static tech.metavm.constant.FieldNames.TYPE_ID;

public class SearchBuilder {

    public static SearchSourceBuilder build(SearchQuery query) {
        SearchSourceBuilder builder = new SearchSourceBuilder();
        builder.from(query.from()).size(query.size());
        builder.query(QueryBuilders.queryStringQuery(buildQueryString(query)));
        return builder;
    }

    public static String buildQueryString(SearchQuery query) {
        String queryString = TENANT_ID +  ":" + query.tenantId() + " AND " + TYPE_ID + ":" + query.typeId();
        if(query.condition() != null) {
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
        return toString(expression.getValue());
    }

    private static String parseBinary(BinaryExpression expression) {
        Operator operator = expression.getOperator();

        if(operator == Operator.EQ || operator == Operator.LIKE || operator == Operator.STARTS_WITH
                || operator == Operator.GT || operator == Operator.GE
                || operator == Operator.LT || operator == Operator.LE
        ) {
            FieldExpression fieldExpr = (FieldExpression) expression.getFirst();
            ConstantExpression constExpr = (ConstantExpression) expression.getSecond();
            Object constValue = constExpr.getValue();
            Column column = fieldExpr.getLastField().getColumn();
            String columnName = operator == Operator.LIKE ? column.fuzzyName() : column.name();
            String value = operator == Operator.STARTS_WITH ? escape((String) constValue) + "*" : toString(constValue);
            return parenthesize(columnName + getEsOperator(operator) + value);
        }
        if(operator == Operator.IN) {
            FieldExpression fieldExpr = (FieldExpression) expression.getFirst();
            ConstantExpression constExpr = (ConstantExpression) expression.getSecond();
            List<Object> values = (List<Object>) constExpr.getValue();
            return parenthesize(
                    NncUtils.join(
                        values, v -> fieldExpr.getLastField().getColumnName() + ":" + toString(v), " OR "
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
