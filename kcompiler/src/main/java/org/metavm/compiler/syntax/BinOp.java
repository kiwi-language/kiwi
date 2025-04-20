package org.metavm.compiler.syntax;

import org.metavm.compiler.generate.Code;
import org.metavm.compiler.type.PrimitiveType;
import org.metavm.compiler.type.Type;

import java.util.EnumSet;

import static org.metavm.compiler.syntax.BinOpFlag.*;

public enum BinOp {
    MUL("*", EnumSet.of(ARITHMETIC)) {
        @Override
        public void apply(Type type, Code code) {
            code.mul(type);
        }
    },
    DIV("/", EnumSet.of(ARITHMETIC)) {
        @Override
        public void apply(Type type, Code code) {
            code.div(type);
        }
    },
    ADD("+", EnumSet.of(ARITHMETIC)) {
        @Override
        public void apply(Type type, Code code) {
            code.add(type);
        }
    },
    SUB("-", EnumSet.of(ARITHMETIC)) {
        @Override
        public void apply(Type type, Code code) {
            code.sub(type);
        }
    },
    MOD("%", EnumSet.of(ARITHMETIC)) {
        @Override
        public void apply(Type type, Code code) {
            code.rem(type);
        }
    },
    GT(">", EnumSet.of(ARITHMETIC)) {
        @Override
        public void apply(Type type, Code code) {
            code.compare(type);
            code.gt();
        }
    },
    GE(">=", EnumSet.of(COMPARISON)) {
        @Override
        public Type getType(Type firstType, Type secondType) {
            return PrimitiveType.BOOL;
        }

        @Override
        public void apply(Type type, Code code) {
            code.compare(type);
            code.ge();
        }
    },
    LT("<", EnumSet.of(COMPARISON)) {
        @Override
        public Type getType(Type firstType, Type secondType) {
            return PrimitiveType.BOOL;
        }

        @Override
        public void apply(Type type, Code code) {
            code.compare(type);
            code.lt();
        }

    },
    LE("<=", EnumSet.of(COMPARISON)) {
        @Override
        public Type getType(Type firstType, Type secondType) {
            return PrimitiveType.BOOL;
        }

        @Override
        public void apply(Type type, Code code) {
            code.compare(type);
            code.le();
        }

    },
    EQ("==", EnumSet.of(COMPARISON)) {
        @Override
        public Type getType(Type firstType, Type secondType) {
            return PrimitiveType.BOOL;
        }

        @Override
        public void apply(Type type, Code code) {
            code.compareEq(type);
        }

    },
    NE("!=", EnumSet.of(COMPARISON)) {
        @Override
        public Type getType(Type firstType, Type secondType) {
            return PrimitiveType.BOOL;
        }

        @Override
        public void apply(Type type, Code code) {
            code.compareNe(type);
        }

    },
    BIT_AND("&", EnumSet.of(ARITHMETIC)) {
        @Override
        public void apply(Type type, Code code) {
            code.bitAnd(type);
        }
    },
    BIT_OR("|", EnumSet.of(ARITHMETIC)) {
        @Override
        public void apply(Type type, Code code) {
            code.bitOr(type);
        }
    },
    BIT_XOR("^", EnumSet.of(ARITHMETIC)) {
        @Override
        public void apply(Type type, Code code) {
            code.bitXor(type);
        }
    },
    SHL("<<", EnumSet.of(ARITHMETIC)) {
        @Override
        public void apply(Type type, Code code) {
            code.shl(type);
        }
    },
    SHR(">>", EnumSet.of(ARITHMETIC)) {
        @Override
        public void apply(Type type, Code code) {
            code.shr(type);
        }
    },
    USHR(">>>", EnumSet.of(ARITHMETIC)) {
        @Override
        public void apply(Type type, Code code) {
            code.ushr(type);
        }
    },
    AND("&&", EnumSet.noneOf(BinOpFlag.class)) {
        @Override
        public void apply(Type type, Code code) {
            throw new UnsupportedOperationException();
        }
    },
    OR("||", EnumSet.noneOf(BinOpFlag.class)) {
        @Override
        public void apply(Type type, Code code) {
            throw new UnsupportedOperationException();
        }
    }

    ;

    private final String op;
    private final EnumSet<BinOpFlag> flags;

    BinOp(String op) {
        this(op, EnumSet.noneOf(BinOpFlag.class));
    }

    BinOp(String op, EnumSet<BinOpFlag> flags) {
        this.op = op;
        this.flags = flags;
    }
    
    public String op() {
        return op;
    }

    public boolean isArithmetic() {
        return flags.contains(ARITHMETIC);
    }

    public boolean isAssignment() {
        return flags.contains(BinOpFlag.ASSIGN);
    }

    public boolean isComparison() {
        return flags.contains(COMPARISON);
    }

    public Type getType(Type firstType, Type secondType) {
        return firstType;
    }

    public abstract void apply(Type type, Code code);

}
