package org.metavm.expression;

import org.metavm.api.EntityType;
import org.metavm.object.instance.core.BooleanValue;
import org.metavm.object.instance.core.LongValue;
import org.metavm.object.instance.core.NumberValue;
import org.metavm.object.instance.core.Value;
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
        public NumberValue evaluate(Value first, Value second) {
            return ((NumberValue) first).mul((NumberValue) second);
        }
    },
    DIVIDE(3, "/", 2, OperatorTypes.BINARY, null) {
        @Override
        public NumberValue evaluate(Value first, Value second) {
            return ((NumberValue) first).div((NumberValue) second);
        }
    },
    MOD(4, "%", 2, OperatorTypes.BINARY, null) {
        @Override
        public NumberValue evaluate(Value first, Value second) {
            return ((NumberValue) first).rem(((NumberValue) second));
        }
    },

    // addition and subtraction
    ADD(5, "+", 3, OperatorTypes.BINARY, null) {
        @Override
        public NumberValue evaluate(Value first, Value second) {
            return ((NumberValue) first).add((NumberValue) second);
        }
    },
    MINUS(6, "-", 3, OperatorTypes.BINARY, null) {
        @Override
        public NumberValue evaluate(Value first, Value second) {
            return ((NumberValue) first).sub((NumberValue) second);
        }
    },
    // SHIFT
    LEFT_SHIFT(23, "<<", 4, OperatorTypes.BINARY, null) {
        @Override
        public LongValue evaluate(Value first, Value second) {
            return ((LongValue) first).leftShift((LongValue) second);
        }
    },
    RIGHT_SHIFT(24, ">>", 4, OperatorTypes.BINARY, null) {
        @Override
        public LongValue evaluate(Value first, Value second) {
            return ((LongValue) first).rightShift((LongValue) second);
        }
    },
    UNSIGNED_RIGHT_SHIFT(25, ">>>", 4, OperatorTypes.BINARY, null) {
        @Override
        public LongValue evaluate(Value first, Value second) {
            return ((LongValue) first).unsignedRightShift((LongValue) second);
        }
    },
    // relational
    GT(7, ">", 5, OperatorTypes.BINARY, Types.getBooleanType()) {
        @Override
        public BooleanValue evaluate(Value first, Value second) {
            return ((NumberValue) first).gt((NumberValue) second);
        }

        @Override
        public BinaryOperator complement() {
            return LE;
        }
    },
    GE(8, ">=", 5, OperatorTypes.BINARY, Types.getBooleanType()) {
        @Override
        public BooleanValue evaluate(Value first, Value second) {
            return ((NumberValue) first).ge((NumberValue) second);
        }

        @Override
        public BinaryOperator complement() {
            return LT;
        }
    },
    LT(9, "<", 5, OperatorTypes.BINARY, Types.getBooleanType()) {
        @Override
        public BooleanValue evaluate(Value first, Value second) {
            return ((NumberValue) first).lt((NumberValue) second);
        }

        @Override
        public BinaryOperator complement() {
            return GE;
        }
    },
    LE(10, "<=", 5, OperatorTypes.BINARY, Types.getBooleanType()) {
        @Override
        public BooleanValue evaluate(Value first, Value second) {
            return ((NumberValue) first).le((NumberValue) second);
        }

        @Override
        public BinaryOperator complement() {
            return GT;
        }
    },

    // equality
    EQ(11, "=", 6, OperatorTypes.BINARY, Types.getBooleanType()) {
        @Override
        public BooleanValue evaluate(Value first, Value second) {
            return Instances.booleanInstance(first.equals(second));
        }

        @Override
        public BinaryOperator complement() {
            return NE;
        }
    },
    NE(12, "!=", 6, OperatorTypes.BINARY, Types.getBooleanType()) {
        @Override
        public BooleanValue evaluate(Value first, Value second) {
            return Instances.booleanInstance(!first.equals(second));
        }

        @Override
        public BinaryOperator complement() {
            return EQ;
        }
    },
    STARTS_WITH(13, "starts with", 6, OperatorTypes.BINARY, Types.getBooleanType()) {
        @Override
        public BooleanValue evaluate(Value first, Value second) {
            return first.toStringInstance().startsWith(second.toStringInstance());
        }
    },
    LIKE(14, "like", 6, OperatorTypes.BINARY, Types.getBooleanType()) {
        @Override
        public BooleanValue evaluate(Value first, Value second) {
            return first.toStringInstance().contains(second.toStringInstance());
        }
    },
    IN(15, "in", 6, OperatorTypes.BINARY, Types.getBooleanType()) {
        @Override
        public BooleanValue evaluate(Value first, Value second) {
            return Instances.booleanInstance((second.resolveArray()).contains(first));
        }
    },
    BITWISE_AND(16, "&", 7, OperatorTypes.BINARY, Types.getLongType()) {
        @Override
        public Value evaluate(Value first, Value second) {
            return ((LongValue) first).bitwiseAnd((LongValue) second);
        }
    },
    BITWISE_XOR(17, "^", 8, OperatorTypes.BINARY, Types.getLongType()) {
        @Override
        public Value evaluate(Value first, Value second) {
            return ((LongValue) first).bitwiseXor((LongValue) second);
        }
    },
    BITWISE_OR(18, "|", 9, OperatorTypes.BINARY, Types.getLongType()) {
        @Override
        public Value evaluate(Value first, Value second) {
            return ((LongValue) first).bitwiseOr((LongValue) second);
        }
    },
    AND(19, "and", 10, OperatorTypes.BINARY, Types.getBooleanType()) {
        @Override
        public BooleanValue evaluate(Value first, Value second) {
            return ((BooleanValue) first).and((BooleanValue) second);
        }
    },
    OR(20, "or", 11, OperatorTypes.BINARY, Types.getBooleanType()) {
        @Override
        public BooleanValue evaluate(Value first, Value second) {
            return ((BooleanValue) first).or((BooleanValue) second);
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

    public abstract Value evaluate(Value first, Value second);

    @Override
    public String toString() {
        return op;
    }
}
