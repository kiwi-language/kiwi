package org.metavm.object.type;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.metavm.object.instance.core.*;
import org.metavm.util.Instances;
import org.metavm.util.NamingUtils;
import org.metavm.util.Password;
import org.metavm.util.Utils;

import javax.annotation.Nullable;
import java.util.Date;

@Slf4j
public enum PrimitiveKind {
    LONG(1, "long", long.class, LongValue.class, TypeCategory.LONG) {
        @Override
        public Value getDefaultValue() {
            return Instances.longInstance(0L);
        }

    },
    DOUBLE(2, "double", double.class, DoubleValue.class, TypeCategory.DOUBLE) {
        @Override
        public Value getDefaultValue() {
            return Instances.doubleInstance(0.0);
        }

    },
    BOOLEAN(4, "boolean", boolean.class, BooleanValue.class, TypeCategory.BOOLEAN) {
        @Override
        public Value getDefaultValue() {
            return Instances.booleanInstance(false);
        }

        @Override
        public Value fromStackValue(Value value) {
            var i = ((IntValue) value).value;
            return i == 0 ? BooleanValue.false_ : BooleanValue.true_;
        }

    },
    TIME(5, "time", Date.class, TimeValue.class, TypeCategory.TIME) {

    },
    PASSWORD(6, "password", Password.class, PasswordValue.class, TypeCategory.PASSWORD),
    VOID(8, "void", void.class, null, TypeCategory.VOID),
    CHAR(9, "char", char.class, CharValue.class, TypeCategory.CHAR) {
        @Override
        public Value fromStackValue(Value value) {
            var i = ((IntValue) value).value;
            return new CharValue((char) i);
        }

    },
    INT(10, "int", int.class, IntValue.class, TypeCategory.INT) {
        @Override
        public Value getDefaultValue() {
            return Instances.intInstance(0);
        }

    },
    FLOAT(11, "float", float.class, FloatValue.class, TypeCategory.FLOAT) {
        @Override
        public Value getDefaultValue() {
            return Instances.floatInstance(0);
        }
    },
    SHORT(12, "short", short.class, ShortValue.class, TypeCategory.SHORT) {
        @Override
        public Value getDefaultValue() {
            return new ShortValue((short) 0);
        }

        @Override
        public Value fromStackValue(Value value) {
            return new ShortValue((short) ((IntValue) value).value);
        }


    },
    BYTE(13, "byte", byte.class, ByteValue.class, TypeCategory.BYTE) {
        @Override
        public Value getDefaultValue() {
            return new ByteValue((byte) 0);
        }

        @Override
        public Value fromStackValue(Value value) {
            return new ByteValue((byte) ((IntValue) value).value);
        }

    };


    private final int code;
    @Getter
    private final String name;
    @Getter
    private final Class<?> javaClass;
    @Getter
    private final Class<? extends Value> instanceClass;
    @Getter
    private final TypeCategory typeCategory;
    @Getter
    private final String typeCode;
    @Getter
    private PrimitiveType type;

    PrimitiveKind(int code, String name, Class<?> javaClass, Class<? extends Value> instanceClass, TypeCategory typeCategory) {
        this.code = code;
        this.name = name;
        this.javaClass = javaClass;
        this.instanceClass = instanceClass;
        this.typeCategory = typeCategory;
        this.typeCode = NamingUtils.firstCharToUpperCase(this.name().toLowerCase());
    }

    public int code() {
        return code;
    }

    public static PrimitiveKind fromCode(int code) {
        return Utils.findRequired(values(), v -> v.code == code);
    }

    void setType(PrimitiveType type) {
        this.type = type;
    }

    public @Nullable Value getDefaultValue() {
        return null;
    }

    public Value fromStackValue(Value value) {
        return value;
    }

}
