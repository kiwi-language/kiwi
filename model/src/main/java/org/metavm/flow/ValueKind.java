package org.metavm.flow;

import org.metavm.entity.EntityType;
import org.metavm.expression.Expression;
import org.metavm.flow.rest.ValueKindCodes;

import java.util.Arrays;

@EntityType
public enum ValueKind {
    NULL(ValueKindCodes.NULL) {
        @Override
        public Value createValue(Expression expression) {
            return Values.nullValue();
        }
    },
    CONSTANT(ValueKindCodes.CONSTANT) {
        @Override
        public Value createValue(Expression expression) {
            return Values.constant(expression);
        }
    },
    REFERENCE(ValueKindCodes.REFERENCE) {
        @Override
        public Value createValue(Expression expression) {
            return Values.reference(expression);
        }
    },
    EXPRESSION(ValueKindCodes.EXPRESSION) {
        @Override
        public Value createValue(Expression expression) {
            return Values.expression(expression);
        }
    };

    private final int code;

    ValueKind(int code) {
        this.code = code;
    }

    public static ValueKind getByCode(int code) {
        return Arrays.stream(values())
                .filter(type -> type.code == code)
                .findAny()
                .orElseThrow(() -> new RuntimeException("Flow node category " + code + " not found"));
    }

    public int code() {
        return code;
    }

    public abstract Value createValue(Expression expression) ;

}
