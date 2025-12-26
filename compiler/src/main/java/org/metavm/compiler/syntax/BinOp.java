package org.metavm.compiler.syntax;

import org.metavm.compiler.generate.Code;
import org.metavm.compiler.type.PrimitiveType;
import org.metavm.compiler.type.Type;
import org.metavm.compiler.type.TypeTags;
import org.metavm.compiler.type.Types;

import java.util.EnumSet;

import static org.metavm.compiler.syntax.BinOpFlag.ARITHMETIC;
import static org.metavm.compiler.syntax.BinOpFlag.COMPARISON;

public enum BinOp {
    MUL("*", EnumSet.of(ARITHMETIC)) {
        @Override
        public void apply(Type type, Code code) {
            code.mul(type);
        }

        @Override
        public boolean check(Type lhsType, Type rhsType) {
            return lhsType.isNumeric() && rhsType.isNumeric();
        }
    },
    DIV("/", EnumSet.of(ARITHMETIC)) {
        @Override
        public void apply(Type type, Code code) {
            code.div(type);
        }

        @Override
        public boolean check(Type lhsType, Type rhsType) {
            return lhsType.isNumeric() && rhsType.isNumeric();
        }
    },
    ADD("+", EnumSet.of(ARITHMETIC)) {
        @Override
        public void apply(Type type, Code code) {
            code.add(type);
        }

        @Override
        public boolean check(Type lhsType, Type rhsType) {
            return lhsType.isNumeric() && rhsType.isNumeric()
                    || lhsType == Types.instance.getStringType() || rhsType == Types.instance.getStringType();
        }

        @Override
        public Type getType(Type firstType, Type secondType) {
            if (TypeTags.isNumeric(firstType.getTag()) && TypeTags.isNumeric(secondType.getTag()))
                return super.getType(firstType, secondType);
            else
                return Types.instance.getStringType();
        }
    },
    SUB("-", EnumSet.of(ARITHMETIC)) {
        @Override
        public void apply(Type type, Code code) {
            code.sub(type);
        }

        @Override
        public boolean check(Type lhsType, Type rhsType) {
            return lhsType.isNumeric() && rhsType.isNumeric();
        }
    },
    MOD("%", EnumSet.of(ARITHMETIC)) {
        @Override
        public void apply(Type type, Code code) {
            code.rem(type);
        }

        @Override
        public boolean check(Type lhsType, Type rhsType) {
            return lhsType.isNumeric() && rhsType.isNumeric();
        }
    },
    GT(">", EnumSet.of(ARITHMETIC)) {
        @Override
        public void apply(Type type, Code code) {
            code.compare(type);
            code.gt();
        }

        @Override
        public boolean check(Type lhsType, Type rhsType) {
            return lhsType.isNumeric() && rhsType.isNumeric();
        }

        @Override
        public Type getType(Type firstType, Type secondType) {
            return PrimitiveType.BOOL;
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

        @Override
        public boolean check(Type lhsType, Type rhsType) {
            return lhsType.isNumeric() && rhsType.isNumeric();
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

        @Override
        public boolean check(Type lhsType, Type rhsType) {
            return lhsType.isNumeric() && rhsType.isNumeric();
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

        @Override
        public boolean check(Type lhsType, Type rhsType) {
            return lhsType.isNumeric() && rhsType.isNumeric();
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

        @Override
        public boolean check(Type lhsType, Type rhsType) {
            return true;
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

        @Override
        public boolean check(Type lhsType, Type rhsType) {
            return true;
        }

    },
    BIT_AND("&", EnumSet.of(ARITHMETIC)) {
        @Override
        public void apply(Type type, Code code) {
            code.bitAnd(type);
        }

        @Override
        public boolean check(Type lhsType, Type rhsType) {
            return lhsType.isInteger() && rhsType.isInteger();
        }
    },
    BIT_OR("|", EnumSet.of(ARITHMETIC)) {
        @Override
        public void apply(Type type, Code code) {
            code.bitOr(type);
        }

        @Override
        public boolean check(Type lhsType, Type rhsType) {
            return lhsType.isInteger() && rhsType.isInteger();
        }
    },
    BIT_XOR("^", EnumSet.of(ARITHMETIC)) {
        @Override
        public void apply(Type type, Code code) {
            code.bitXor(type);
        }

        @Override
        public boolean check(Type lhsType, Type rhsType) {
            return lhsType.isInteger() && rhsType.isInteger();
        }
    },
    SHL("<<", EnumSet.of(ARITHMETIC)) {
        @Override
        public void apply(Type type, Code code) {
            code.shl(type);
        }

        @Override
        public boolean check(Type lhsType, Type rhsType) {
            return lhsType.isInteger() && rhsType.isInteger() && rhsType != PrimitiveType.LONG;
        }
    },
    SHR(">>", EnumSet.of(ARITHMETIC)) {
        @Override
        public void apply(Type type, Code code) {
            code.shr(type);
        }

        @Override
        public boolean check(Type lhsType, Type rhsType) {
            return lhsType.isInteger() && rhsType.isInteger() && rhsType != PrimitiveType.LONG;
        }
    },
    USHR(">>>", EnumSet.of(ARITHMETIC)) {
        @Override
        public void apply(Type type, Code code) {
            code.ushr(type);
        }

        @Override
        public boolean check(Type lhsType, Type rhsType) {
            return lhsType.isInteger() && rhsType.isInteger() && rhsType != PrimitiveType.LONG;
        }
    },
    AND("&&", EnumSet.noneOf(BinOpFlag.class)) {
        @Override
        public void apply(Type type, Code code) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean check(Type lhsType, Type rhsType) {
            return lhsType.isBool() && rhsType.isBool();
        }
    },
    OR("||", EnumSet.noneOf(BinOpFlag.class)) {
        @Override
        public void apply(Type type, Code code) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean check(Type lhsType, Type rhsType) {
            return lhsType.isBool() && rhsType.isBool();
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
        return firstType.getTag() > secondType.getTag() ? firstType : secondType;
    }

    public abstract void apply(Type type, Code code);

    public abstract boolean check(Type lhsType, Type rhsType);

}
