package org.metavm.object.instance.search;

import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.metavm.constant.FieldNames;
import org.metavm.expression.*;
import org.metavm.object.instance.core.NumberValue;
import org.metavm.object.instance.core.StringReference;
import org.metavm.object.instance.core.Value;
import org.metavm.object.type.Field;
import org.metavm.object.type.Klass;
import org.metavm.util.Column;
import org.metavm.util.InternalException;
import org.metavm.util.Utils;

import javax.annotation.Nullable;
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
                    Utils.join(query.types(), t -> TYPE + ":\"" + t + "\"", " OR ") + ")";
            queryString += " AND " + typeCond;
        }
        if (query.condition() != null) {
            queryString += " AND " + query.condition().build();
        }
        return queryString;
    }

    public static SearchCondition buildSearchCondition(Expression expression) {
        return parse(ExpressionSimplifier.simplify(expression));
    }

    private static SearchCondition parse(Expression expression) {
        if (expression instanceof BinaryExpression binaryExpression) {
            return parseBinary(binaryExpression);
        }
        if (expression instanceof FunctionExpression functionExpression) {
            return parseFunction(functionExpression);
        }
        throw new RuntimeException("Unsupported expression: " + expression.getClass().getName());
    }

    private static SearchCondition parseFunction(FunctionExpression expression) {
        var func = expression.getFunction();
        if (func == Func.STARTS_WITH || func == Func.CONTAINS) {
            Expression fieldExpr = expression.getArguments().getFirst();
            ConstantExpression constExpr = (ConstantExpression) expression.getArguments().get(1);
            Value constValue = constExpr.getValue();
            SearchField searchField = getColumn(fieldExpr);
            if (func == Func.STARTS_WITH)
                return new PrefixSearchCondition(searchField.name(), (StringReference) constValue);
            else
                return new MatchSearchCondition(searchField.fuzzyName(), constValue);
        } else {
            throw new InternalException("Unsupported function " + func);
        }
    }

    private static SearchCondition parseBinary(BinaryExpression expression) {
        var operator = expression.getOperator();
        if (operator == BinaryOperator.AND) {
            return new AndSearchCondition(
                    List.of(
                            parse(expression.getLeft()),
                            parse(expression.getRight())
                    )
            );
        } else if (operator == BinaryOperator.OR) {
            return new OrSearchCondition(
                    List.of(
                            parse(expression.getLeft()),
                            parse(expression.getRight())
                    )
            );
        }
        var fieldExpr = expression.getVariableComponent();
        var constExpr = expression.getConstantComponent();
        var constValue = constExpr.getValue();
        var searchField = getColumn(fieldExpr);
        var esField = searchField.name();
        return switch (expression.getOperator()) {
            case EQ -> new MatchSearchCondition(esField, constValue);
            case LT -> new LtSearchCondition(esField, (NumberValue) constValue);
            case GT -> new GtSearchCondition(esField, (NumberValue) constValue);
            case LE -> new LeSearchCondition(esField, (NumberValue) constValue);
            case GE -> new GeSearchCondition(esField, (NumberValue) constValue);
            case IN -> new InSearchCondition(esField, constValue.resolveArray().getElements());
            default -> throw new IllegalStateException("Unsupported operator for search: " + operator.name());
        };
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

    public static final Set<Character> SPECIAL_CHARS = Set.of(
            '+', '-', '=', '>', '<', '!', '(', ')', '{', '}',
            '[', ']', '^', '\'', '~', '*', '?', ':', '\\', '/', ' '
    );

    public static final Set<Character> SPECIAL_DOUBLE_CHARS = Set.of(
            '|', '&'
    );

    public static String toString(Object value) {
        if (value instanceof String string) {
            return "\"" + escape(string) + "\"";
        }
        return value != null ? escape(value.toString()) : "null";
    }

    public static String escape(String value) {
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
