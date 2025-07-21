package org.metavm.compiler.syntax;

import org.metavm.compiler.generate.Code;
import org.metavm.compiler.type.Type;

public enum PrefixOp {
    INC("++") {
        @Override
        public void apply(Type type, Code code) {
            code.inc(type);
        }

        @Override
        public boolean check(Type type) {
            return type.isNumeric();
        }
    },
    DEC("--") {
        @Override
        public void apply(Type type, Code code) {
            code.dec(type);
        }

        @Override
        public boolean check(Type type) {
            return type.isNumeric();
        }
    },
    POS("+") {
        @Override
        public void apply(Type type, Code code) {
        }

        @Override
        public boolean check(Type type) {
            return type.isNumeric();
        }
    },
    NEGATE("-") {
        @Override
        public void apply(Type type, Code code) {
            code.neg(type);
        }

        @Override
        public boolean check(Type type) {
            return type.isNumeric();
        }
    },
    NOT("!") {
        @Override
        public void apply(Type type, Code code) {
            code.not();
        }

        @Override
        public boolean check(Type type) {
            return type.isBool();
        }
    },
    BIT_NOT("~") {
        @Override
        public void apply(Type type, Code code) {
            code.bitNot(type);
        }

        @Override
        public boolean check(Type type) {
            return type.isInteger();
        }
    },

    ;

    private final String op;

    PrefixOp(String op) {
        this.op = op;
    }

    public String op() {
        return op;
    }

    public Type getType(Type type) {
        return type;
    }

    public abstract void apply(Type type, Code code);

    public abstract boolean check(Type type);

}
