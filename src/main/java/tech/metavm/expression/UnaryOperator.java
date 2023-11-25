package tech.metavm.expression;

import tech.metavm.entity.EntityType;
import tech.metavm.entity.EnumConstant;
import tech.metavm.entity.ModelDefRegistry;
import tech.metavm.object.instance.core.BooleanInstance;
import tech.metavm.object.instance.core.Instance;
import tech.metavm.object.instance.core.NumberInstance;
import tech.metavm.object.type.Type;
import tech.metavm.util.InstanceUtils;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;

@EntityType("一元运算符")
public enum UnaryOperator {

    @EnumConstant("非")
    NOT(1, "!", 1, true, Boolean.class) {
        @Override
        public Instance evaluate(Instance operand) {
            return ((BooleanInstance) operand).not();
        }
    },
    @EnumConstant("正")
    POS(35, "+", 1, true, null) {
        @Override
        public Instance evaluate(Instance operand) {
            return operand;
        }

        @Override
        public UnaryOperator complement() {
            return NEG;
        }
    },
    @EnumConstant("负")
    NEG(36, "-", 1, true, null) {
        @Override
        public Instance evaluate(Instance operand) {
            return ((NumberInstance) operand).negate();
        }

        @Override
        public UnaryOperator complement() {
            return POS;
        }
    },
    @EnumConstant("为空")
    IS_NULL(16, "IS NULL", 6, false, Boolean.class) {
        @Override
        public Instance evaluate(Instance operand) {
            return InstanceUtils.booleanInstance(operand == null);
        }

        @Override
        public UnaryOperator complement() {
            return IS_NOT_NULL;
        }
    },
    @EnumConstant("不为空")
    IS_NOT_NULL(17, "IS NOT NULL", 6, false, Boolean.class) {
        @Override
        public Instance evaluate(Instance operand) {
            return InstanceUtils.booleanInstance(operand != null);
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

    public abstract Instance evaluate(Instance operand);

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
