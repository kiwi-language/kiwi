package org.metavm.compiler.syntax;

import org.metavm.compiler.generate.Code;
import org.metavm.compiler.type.Type;

public enum PrefixOp {
    INC("++") {
        @Override
        public void apply(Type type, Code code) {
            code.inc(type);
        }
    },
    DEC("--") {
        @Override
        public void apply(Type type, Code code) {
            code.dec(type);
        }
    },
    POS("+") {
        @Override
        public void apply(Type type, Code code) {
        }
    },
    NEGATE("-") {
        @Override
        public void apply(Type type, Code code) {
            code.neg(type);
        }
    },
    NOT("!") {
        @Override
        public void apply(Type type, Code code) {
            code.not();
        }
    },
    BIT_NOT("~") {
        @Override
        public void apply(Type type, Code code) {
            code.bitNot(type);
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
}
