package org.metavm.expression;

import org.metavm.api.Entity;
import org.metavm.entity.ModelDefRegistry;
import org.metavm.object.instance.core.BooleanValue;
import org.metavm.object.instance.core.LongValue;
import org.metavm.object.instance.core.NumberValue;
import org.metavm.object.instance.core.Value;
import org.metavm.object.type.Type;
import org.metavm.util.Instances;
import org.metavm.util.NncUtils;

import javax.annotation.Nullable;

@Entity
public enum UnaryOperator {

    NOT(1, "!", 1, true, Boolean.class) {
        @Override
        public Value evaluate(Value operand) {
            return ((BooleanValue) operand).not();
        }
    },
    POS(35, "+", 1, true, null) {
        @Override
        public Value evaluate(Value operand) {
            return operand;
        }

        @Override
        public UnaryOperator complement() {
            return NEG;
        }
    },
    NEG(36, "-", 1, true, null) {
        @Override
        public Value evaluate(Value operand) {
            return ((NumberValue) operand).negate();
        }

        @Override
        public UnaryOperator complement() {
            return POS;
        }
    },
    BITWISE_COMPLEMENT(37, "~", 1 , true, Long.class) {
        @Override
        public Value evaluate(Value operand) {
            return ((LongValue) operand).bitNot();
        }
    },
    IS_NULL(16, "is null", 6, false, Boolean.class) {
        @Override
        public Value evaluate(Value operand) {
            return Instances.intInstance(operand == null);
        }

        @Override
        public UnaryOperator complement() {
            return IS_NOT_NULL;
        }
    },
    IS_NOT_NULL(17, "is not null", 6, false, Boolean.class) {
        @Override
        public Value evaluate(Value operand) {
            return Instances.intInstance(operand != null);
        }

        @Override
        public UnaryOperator complement() {
            return IS_NULL;
        }
    },

    ;

    private final int code;
    private final String operator;
    private final int precedence;
    private final boolean isPrefix;
    private final Class<?> resultType;

    UnaryOperator(int code, String operator, int precedence, boolean isPrefix, @Nullable Class<?> resultClass) {
        this.code = code;
        this.operator = operator;
        this.precedence = precedence;
        this.isPrefix = isPrefix;
        this.resultType = resultClass;
    }

    public abstract Value evaluate(Value operand);

    public int code() {
        return this.code;
    }

    public String operator() {
        return operator;
    }

    public Type resultType() {
        return NncUtils.get(resultType, ModelDefRegistry::getType);
    }

    public boolean isPrefix() {
        return isPrefix;
    }

    public int precedence() {
        return precedence;
    }

    public UnaryOperator complement() {
        throw new UnsupportedOperationException();
    }

}
