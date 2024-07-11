package org.metavm.expression;

import org.metavm.api.EntityType;
import org.metavm.object.instance.core.BooleanInstance;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.LongInstance;
import org.metavm.object.instance.core.NumberInstance;
import org.metavm.object.instance.query.OperatorTypes;
import org.metavm.object.type.Type;
import org.metavm.object.type.Types;
import org.metavm.util.Instances;
import org.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.Arrays;


@EntityType
public enum BinaryOperator {

    // Multiply and division
    MULTIPLY(2, "*", 2, OperatorTypes.BINARY, null) {
        @Override
        public NumberInstance evaluate(Instance first, Instance second) {
            return ((NumberInstance) first).mul((NumberInstance) second);
        }
    },
    DIVIDE(3, "/", 2, OperatorTypes.BINARY, null) {
        @Override
        public NumberInstance evaluate(Instance first, Instance second) {
            return ((NumberInstance) first).div((NumberInstance) second);
        }
    },
    MOD(4, "%", 2, OperatorTypes.BINARY, null) {
        @Override
        public NumberInstance evaluate(Instance first, Instance second) {
            return ((NumberInstance) first).mod(((NumberInstance) second));
        }
    },

    // addition and subtraction
    ADD(5, "+", 3, OperatorTypes.BINARY, null) {
        @Override
        public NumberInstance evaluate(Instance first, Instance second) {
            return ((NumberInstance) first).add((NumberInstance) second);
        }
    },
    MINUS(6, "-", 3, OperatorTypes.BINARY, null) {
        @Override
        public NumberInstance evaluate(Instance first, Instance second) {
            return ((NumberInstance) first).minus((NumberInstance) second);
        }
    },

    // SHIFT
    LEFT_SHIFT(23, "<<", 4, OperatorTypes.BINARY, null) {
        @Override
        public LongInstance evaluate(Instance first, Instance second) {
            return ((LongInstance) first).leftShift((LongInstance) second);
        }
    },
    RIGHT_SHIFT(24, ">>", 4, OperatorTypes.BINARY, null) {
        @Override
        public LongInstance evaluate(Instance first, Instance second) {
            return ((LongInstance) first).rightShift((LongInstance) second);
        }
    },
    UNSIGNED_RIGHT_SHIFT(25, ">>>", 4, OperatorTypes.BINARY, null) {
        @Override
        public LongInstance evaluate(Instance first, Instance second) {
            return ((LongInstance) first).unsignedRightShift((LongInstance) second);
        }
    },

    // relational
    GT(7, ">", 5, OperatorTypes.BINARY, Types.getBooleanType()) {
        @Override
        public BooleanInstance evaluate(Instance first, Instance second) {
            return ((NumberInstance) first).gt((NumberInstance) second);
        }

        @Override
        public BinaryOperator complement() {
            return LE;
        }
    },
    GE(8, ">=", 5, OperatorTypes.BINARY, Types.getBooleanType()) {
        @Override
        public BooleanInstance evaluate(Instance first, Instance second) {
            return ((NumberInstance) first).ge((NumberInstance) second);
        }

        @Override
        public BinaryOperator complement() {
            return LT;
        }
    },
    LT(9, "<", 5, OperatorTypes.BINARY, Types.getBooleanType()) {
        @Override
        public BooleanInstance evaluate(Instance first, Instance second) {
            return ((NumberInstance) first).lt((NumberInstance) second);
        }

        @Override
        public BinaryOperator complement() {
            return GE;
        }
    },
    LE(10, "<=", 5, OperatorTypes.BINARY, Types.getBooleanType()) {
        @Override
        public BooleanInstance evaluate(Instance first, Instance second) {
            return ((NumberInstance) first).le((NumberInstance) second);
        }

        @Override
        public BinaryOperator complement() {
            return GT;
        }
    },

    // equality
    EQ(11, "=", 6, OperatorTypes.BINARY, Types.getBooleanType()) {
        @Override
        public BooleanInstance evaluate(Instance first, Instance second) {
            return Instances.booleanInstance(first.equals(second));
        }

        @Override
        public BinaryOperator complement() {
            return NE;
        }
    },
    NE(12, "!=", 6, OperatorTypes.BINARY, Types.getBooleanType()) {
        @Override
        public BooleanInstance evaluate(Instance first, Instance second) {
            return Instances.booleanInstance(!first.equals(second));
        }

        @Override
        public BinaryOperator complement() {
            return EQ;
        }
    },
    STARTS_WITH(13, "starts with", 6, OperatorTypes.BINARY, Types.getBooleanType()) {
        @Override
        public BooleanInstance evaluate(Instance first, Instance second) {
            return first.toStringInstance().startsWith(second.toStringInstance());
        }
    },
    LIKE(14, "like", 6, OperatorTypes.BINARY, Types.getBooleanType()) {
        @Override
        public BooleanInstance evaluate(Instance first, Instance second) {
            return first.toStringInstance().contains(second.toStringInstance());
        }
    },
    IN(15, "in", 6, OperatorTypes.BINARY, Types.getBooleanType()) {
        @Override
        public BooleanInstance evaluate(Instance first, Instance second) {
            return Instances.booleanInstance((second.resolveArray()).contains(first));
        }
    },
    AND(20, "and", 7, OperatorTypes.BINARY, Types.getBooleanType()) {
        @Override
        public BooleanInstance evaluate(Instance first, Instance second) {
            return ((BooleanInstance) first).and((BooleanInstance) second);
        }
    },
    OR(21, "or", 8, OperatorTypes.BINARY, Types.getBooleanType()) {
        @Override
        public BooleanInstance evaluate(Instance first, Instance second) {
            return ((BooleanInstance) first).or((BooleanInstance) second);
        }
    },

    ;

    private final int code;
    private final String op;
    private final int precedence;
    private final int type;
    private final @Nullable Type resultType;

    BinaryOperator(int code, String op, int precedence, int type, @Nullable Type resultType) {
        this.code = code;
        this.op = op;
        this.precedence = precedence;
        this.type = type;
        this.resultType = resultType;
    }

    public static BinaryOperator getByCode(int code) {
        return NncUtils.findRequired(values(), op -> op.code == code);
    }

    public static BinaryOperator getByOp(String op) {
        return getByOp(op, null);
    }

    public static BinaryOperator getByOp(String op, Integer type) {
        return Arrays.stream(values())
                .filter(operator -> operator.op.equalsIgnoreCase(op) && (type == null || type == operator.type))
                .findAny()
                .orElseThrow(() -> new RuntimeException("Invalid operator '" + op + "'"));
    }

    public static boolean isOperator(String op) {
        return Arrays.stream(values()).anyMatch(operator -> operator.op.equalsIgnoreCase(op));
    }

    public @Nullable Type resultType() {
        return resultType;
    }

    public int precedence() {
        return precedence;
    }

    public boolean isPostfix() {
        return type == OperatorTypes.POSTFIX;
    }

    public boolean isPrefix() {
        return type == OperatorTypes.PREFIX;
    }

    public boolean isUnary() {
        return isPrefix() || isPostfix();
    }

    public boolean isBinary() {
        return type == OperatorTypes.BINARY;
    }

    public int code() {
        return this.code;
    }

    public BinaryOperator complement() {
        throw new UnsupportedOperationException();
    }

    public abstract Instance evaluate(Instance first, Instance second);

    @Override
    public String toString() {
        return op;
    }
}
