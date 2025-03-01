package org.metavm.compiler.syntax;

import org.metavm.compiler.generate.Code;
import org.metavm.compiler.type.PrimitiveType;
import org.metavm.compiler.type.Type;
import org.metavm.compiler.type.UnionType;

public enum PostfixOp {
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
    NONNULL("!!") {
        @Override
        public Type getType(Type type) {
            return type.getUnderlyingType();
        }

        @Override
        public void apply(Type type, Code code) {
            code.nonnull();
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

}
