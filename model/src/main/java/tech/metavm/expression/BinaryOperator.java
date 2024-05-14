package tech.metavm.expression;

import tech.metavm.entity.EntityType;
import tech.metavm.entity.EnumConstant;
import tech.metavm.entity.StandardTypes;
import tech.metavm.object.instance.core.*;
import tech.metavm.object.instance.query.OperatorTypes;
import tech.metavm.object.type.Type;
import tech.metavm.util.Instances;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.Arrays;


@EntityType("二元运算符")
public enum BinaryOperator {

    // Multiply and division
    @EnumConstant("乘")
    MULTIPLY(2, "*", 2, OperatorTypes.BINARY, null) {
        @Override
        public NumberInstance evaluate(Instance first, Instance second) {
            return ((NumberInstance) first).mul((NumberInstance) second);
        }
    },
    @EnumConstant("除")
    DIVIDE(3, "/", 2, OperatorTypes.BINARY, null) {
        @Override
        public NumberInstance evaluate(Instance first, Instance second) {
            return ((NumberInstance) first).div((NumberInstance) second);
        }
    },
    @EnumConstant("模")
    MOD(4, "%", 2, OperatorTypes.BINARY, null) {
        @Override
        public NumberInstance evaluate(Instance first, Instance second) {
            return ((NumberInstance) first).mod(((NumberInstance) second));
        }
    },

    // addition and subtraction
    @EnumConstant("加")
    ADD(5, "+", 3, OperatorTypes.BINARY, null) {
        @Override
        public NumberInstance evaluate(Instance first, Instance second) {
            return ((NumberInstance) first).add((NumberInstance) second);
        }
    },
    @EnumConstant("减")
    MINUS(6, "-", 3, OperatorTypes.BINARY, null) {
        @Override
        public NumberInstance evaluate(Instance first, Instance second) {
            return ((NumberInstance) first).minus((NumberInstance) second);
        }
    },

    // SHIFT
    @EnumConstant("左移")
    LEFT_SHIFT(23, "<<", 4, OperatorTypes.BINARY, null) {
        @Override
        public LongInstance evaluate(Instance first, Instance second) {
            return ((LongInstance) first).leftShift((LongInstance) second);
        }
    },
    @EnumConstant("右移")
    RIGHT_SHIFT(24, ">>", 4, OperatorTypes.BINARY, null) {
        @Override
        public LongInstance evaluate(Instance first, Instance second) {
            return ((LongInstance) first).rightShift((LongInstance) second);
        }
    },
    @EnumConstant("无符号右移")
    UNSIGNED_RIGHT_SHIFT(25, ">>>", 4, OperatorTypes.BINARY, null) {
        @Override
        public LongInstance evaluate(Instance first, Instance second) {
            return ((LongInstance) first).unsignedRightShift((LongInstance) second);
        }
    },

    // relational
    @EnumConstant("大于")
    GT(7, ">", 5, OperatorTypes.BINARY, StandardTypes.getBooleanType()) {
        @Override
        public BooleanInstance evaluate(Instance first, Instance second) {
            return ((NumberInstance) first).gt((NumberInstance) second);
        }

        @Override
        public BinaryOperator complement() {
            return LE;
        }
    },
    @EnumConstant("大于等于")
    GE(8, ">=", 5, OperatorTypes.BINARY, StandardTypes.getBooleanType()) {
        @Override
        public BooleanInstance evaluate(Instance first, Instance second) {
            return ((NumberInstance) first).ge((NumberInstance) second);
        }

        @Override
        public BinaryOperator complement() {
            return LT;
        }
    },
    @EnumConstant("小于")
    LT(9, "<", 5, OperatorTypes.BINARY, StandardTypes.getBooleanType()) {
        @Override
        public BooleanInstance evaluate(Instance first, Instance second) {
            return ((NumberInstance) first).lt((NumberInstance) second);
        }

        @Override
        public BinaryOperator complement() {
            return GE;
        }
    },
    @EnumConstant("小于等于")
    LE(10, "<=", 5, OperatorTypes.BINARY, StandardTypes.getBooleanType()) {
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
    @EnumConstant("等于")
    EQ(11, "=", 6, OperatorTypes.BINARY, StandardTypes.getBooleanType()) {
        @Override
        public BooleanInstance evaluate(Instance first, Instance second) {
            return Instances.booleanInstance(first.equals(second));
        }

        @Override
        public BinaryOperator complement() {
            return NE;
        }
    },
    @EnumConstant("不等于")
    NE(12, "!=", 6, OperatorTypes.BINARY, StandardTypes.getBooleanType()) {
        @Override
        public BooleanInstance evaluate(Instance first, Instance second) {
            return Instances.booleanInstance(!first.equals(second));
        }

        @Override
        public BinaryOperator complement() {
            return EQ;
        }
    },
    @EnumConstant("判断文本前缀")
    STARTS_WITH(13, "starts with", 6, OperatorTypes.BINARY, StandardTypes.getBooleanType()) {
        @Override
        public BooleanInstance evaluate(Instance first, Instance second) {
            return first.toStringInstance().startsWith(second.toStringInstance());
        }
    },
    @EnumConstant("模糊匹配")
    LIKE(14, "like", 6, OperatorTypes.BINARY, StandardTypes.getBooleanType()) {
        @Override
        public BooleanInstance evaluate(Instance first, Instance second) {
            return first.toStringInstance().contains(second.toStringInstance());
        }
    },
    @EnumConstant("包含于数组")
    IN(15, "in", 6, OperatorTypes.BINARY, StandardTypes.getBooleanType()) {
        @Override
        public BooleanInstance evaluate(Instance first, Instance second) {
            return Instances.booleanInstance(((ArrayInstance) second).contains(first));
        }
    },
    @EnumConstant("且")
    AND(20, "and", 7, OperatorTypes.BINARY, StandardTypes.getBooleanType()) {
        @Override
        public BooleanInstance evaluate(Instance first, Instance second) {
            return ((BooleanInstance) first).and((BooleanInstance) second);
        }
    },
    @EnumConstant("或")
    OR(21, "or", 8, OperatorTypes.BINARY, StandardTypes.getBooleanType()) {
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
