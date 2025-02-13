package org.metavm.object.type;

import org.metavm.util.Utils;

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
        Utils.requireNotEmpty(typeArguments);
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
        expression = TypeExpressions.getArrayListType(expression);
        return this;
    }

    public String build() {
        return expression;
    }

}
