package tech.metavm.object.type;

import tech.metavm.util.NncUtils;

import java.util.List;

public class TypeExpressionBuilder {

    public static TypeExpressionBuilder fromKlassId(String klassId) {
        return new TypeExpressionBuilder(TypeExpressions.getClassType(klassId));
    }

    private String expression;

    private TypeExpressionBuilder(String initialExpression) {
        this.expression = initialExpression;
    }

    public TypeExpressionBuilder parameterize(List<String> typeArguments) {
        NncUtils.requireNotEmpty(typeArguments);
        expression += "<" + String.join(",", typeArguments) + ">";
        return this;
    }

    public TypeExpressionBuilder readWriteArray() {
        expression += "[rw]";
        return this;
    }

    public TypeExpressionBuilder list() {
        expression = TypeExpressions.getListType(expression);
        return this;
    }

    public TypeExpressionBuilder readWriteList() {
        expression = TypeExpressions.getReadWriteListType(expression);
        return this;
    }

    public String build() {
        return expression;
    }

}
