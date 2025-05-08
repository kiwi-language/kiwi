package org.metavm.compiler.type;

import org.metavm.compiler.element.ElementTable;
import org.metavm.compiler.element.ElementWriter;
import org.metavm.compiler.element.Func;
import org.metavm.compiler.syntax.PrimitiveTypeNode;
import org.metavm.compiler.syntax.TypeNode;
import org.metavm.compiler.syntax.TypeTag;
import org.metavm.util.MvOutput;
import org.metavm.util.NamingUtils;

import javax.annotation.Nullable;

import static org.metavm.compiler.type.TypeTags.*;
import static org.metavm.util.WireTypes.*;

public enum PrimitiveType implements Type {

    NULL(NULL_TYPE, TAG_NULL),
    BYTE(BYTE_TYPE, TAG_BYTE) {
        @Override
        public Type toStackType() {
            return INT;
        }

        @Override
        public boolean widensTo(PrimitiveType that) {
            return that == SHORT || that == INT || that == LONG || that == FLOAT || that == DOUBLE;
        }
    },
    SHORT(SHORT_TYPE, TAG_SHORT) {
        @Override
        public Type toStackType() {
            return INT;
        }

        @Override
        public boolean widensTo(PrimitiveType that) {
            return that == INT || that == LONG || that == FLOAT || that == DOUBLE;
        }

    },
    INT(INT_TYPE, TAG_INT) {

        @Override
        public boolean widensTo(PrimitiveType that) {
            return that == LONG || that == FLOAT || that == DOUBLE;
        }

    },
    LONG(LONG_TYPE, TAG_LONG) {

        @Override
        public boolean widensTo(PrimitiveType that) {
            return that == FLOAT || that == DOUBLE;
        }

    },
    FLOAT(FLOAT_TYPE, TAG_FLOAT) {

        @Override
        public boolean widensTo(PrimitiveType that) {
            return that == DOUBLE;
        }
    },
    DOUBLE(DOUBLE_TYPE, TAG_DOUBLE),
    CHAR(CHAR_TYPE, TAG_CHAR) {
        @Override
        public Type toStackType() {
            return INT;
        }

        @Override
        public boolean widensTo(PrimitiveType that) {
            return that == INT || that == LONG || that == FLOAT || that == DOUBLE;
        }
    },
    BOOL(BOOLEAN_TYPE, TAG_BOOL) {
        @Override
        public Type toStackType() {
            return INT;
        }

        @Override
        public String getInternalName(@org.jetbrains.annotations.Nullable Func current) {
            return "Boolean";
        }
    },
//    STRING(STRING_TYPE, TAG_STRING) {
//        @Override
//        public void write(MvOutput output) {
//            output.write(CLASS_TYPE);
//            output.write(SymbolRefs.KLASS);
//            output.writeUTF("java.lang.String");
//        }
//
//        @Override
//        public String getInternalName(@Nullable Func current) {
//            return "java.lang.String";
//        }
//    },
    VOID(VOID_TYPE, TAG_VOID) {
        @Override
        public boolean isVoid() {
            return true;
        }
    },
    ANY(ANY_TYPE, TAG_ANY) {
        @Override
        public boolean isAssignableFrom(Type type) {
            type = type.getUpperBound();
            return type != NULL;
        }
    },
    NEVER(NEVER_TYPE, TAG_NEVER),
    TIME(TIME_TYPE, TAG_TIME),
    PASSWORD(PASSWORD_TYPE, TAG_PASSWORD),

    ;

    private final int constantTag;
    private final int tag;
    private final Closure closure;

    PrimitiveType(int constantTag, int tag) {
        this.constantTag = constantTag;
        this.tag = tag;
        closure = Closure.of(this);
    }

    public boolean widensTo(PrimitiveType that) {
        return false;
    }

    @Override
    public void writeType(ElementWriter writer) {
        writer.write(name().toLowerCase());
    }

    @Override
    public boolean isAssignableFrom(Type type) {
        type = type.getUpperBound();
        return type == this || type == NEVER;
    }

    @Override
    public <R> R accept(TypeVisitor<R> visitor) {
        return visitor.visitPrimitiveType(this);
    }

    @Override
    public int getTag() {
        return tag;
    }

    @Override
    public String getInternalName(@Nullable Func current) {
        return NamingUtils.firstCharToUpperCase(name().toLowerCase());
    }

    @Override
    public void write(MvOutput output) {
        output.write(constantTag);
    }

    @Override
    public boolean isPrimitive() {
        return this == PrimitiveType.BYTE || this == PrimitiveType.SHORT
                || this == PrimitiveType.INT || this == PrimitiveType.LONG
                || this == PrimitiveType.FLOAT || this == PrimitiveType.DOUBLE
                || this == PrimitiveType.CHAR || this == PrimitiveType.BOOL;
    }

    @Override
    public TypeNode makeNode() {
        var node = new PrimitiveTypeNode(TypeTag.valueOf(name()));
        node.setType(this);
        return node;
    }

    @Override
    public Closure getClosure() {
        return closure;
    }

    @Override
    public ElementTable getTable() {
        return ElementTable.empty;
    }
}
