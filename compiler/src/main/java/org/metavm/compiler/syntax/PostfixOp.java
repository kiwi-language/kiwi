package org.metavm.compiler.syntax;

import org.metavm.compiler.generate.Code;
import org.metavm.compiler.type.Type;

public enum PostfixOp {
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
    NONNULL("!!") {
        @Override
        public Type getType(Type type) {
            return type.getUnderlyingType();
        }

        @Override
        public void apply(Type type, Code code) {
            code.nonnull();
        }

        @Override
        public boolean check(Type type) {
            return true;
        }
    };


    private final String op;

    PostfixOp(String op) {
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
