package org.metavm.object.type;

import lombok.extern.slf4j.Slf4j;
import org.metavm.api.NativeApi;
import org.metavm.entity.natives.HybridValueHolder;
import org.metavm.entity.natives.ValueHolder;
import org.metavm.entity.natives.ValueHolderOwner;
import org.metavm.object.instance.core.*;
import org.metavm.util.Instances;
import org.metavm.util.NamingUtils;
import org.metavm.util.Password;
import org.metavm.util.Utils;

import javax.annotation.Nullable;
import java.util.Date;

@Slf4j
public enum PrimitiveKind implements ValueHolderOwner<Klass> {
    LONG(1, "long", long.class, LongValue.class, TypeCategory.LONG) {
        @Override
        public Value getDefaultValue() {
            return Instances.longInstance(0L);
        }

        @NativeApi
        public static Value compareTo(Value self, Value that) {
            var s1 = (LongValue) self;
            var s2 = (LongValue) that;
            return Instances.intInstance(s1.compareTo(s2));
        }

        @NativeApi
        public static Value intValue(Value self) {
            var v = ((LongValue) self).value;
            return Instances.intInstance((int) v);
        }

        @NativeApi
        public static Value longValue(Value self) {
            return self;
        }

        @NativeApi
        public static Value floatValue(Value self) {
            var v = ((LongValue) self).value;
            return Instances.floatInstance((float) v);
        }

        @NativeApi
        public static Value doubleValue(Value self) {
            var v = ((LongValue) self).value;
            return Instances.doubleInstance(v);
        }

    },
    DOUBLE(2, "double", double.class, DoubleValue.class, TypeCategory.DOUBLE) {
        @Override
        public Value getDefaultValue() {
            return Instances.doubleInstance(0.0);
        }

        @NativeApi
        public static Value compareTo(Value self, Value that) {
            var s1 = (DoubleValue) self;
            var s2 = (DoubleValue) that;
            return Instances.intInstance(s1.compareTo(s2));
        }

        @NativeApi
        public static Value intValue(Value self) {
            var v = ((DoubleValue) self).value;
            return Instances.intInstance((int) v);
        }

        @NativeApi
        public static Value longValue(Value self) {
            var v = ((DoubleValue) self).value;
            return Instances.longInstance((long) v);
        }

        @NativeApi
        public static Value floatValue(Value self) {
            var v = ((DoubleValue) self).value;
            return Instances.floatInstance((float) v);
        }

        @NativeApi
        public static Value doubleValue(Value self) {
            return self;
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

        @NativeApi
        public static Value compareTo(Value self, Value that) {
            var s1 = (TimeValue) self;
            var s2 = (TimeValue) that;
            return Instances.intInstance(s1.compareTo(s2));
        }

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

        @NativeApi
        public static Value compareTo(Value self, Value that) {
            var s1 = (IntValue) self;
            var s2 = (IntValue) that;
            return Instances.intInstance(s1.compareTo(s2));
        }

        @NativeApi
        public static Value intValue(Value self) {
            return self;
        }

        @NativeApi
        public static Value longValue(Value self) {
            var v = ((IntValue) self).value;
            return Instances.longInstance(v);
        }

        @NativeApi
        public static Value floatValue(Value self) {
            var v = ((IntValue) self).value;
            return Instances.floatInstance((float) v);
        }

        @NativeApi
        public static Value doubleValue(Value self) {
            var v = ((IntValue) self).value;
            return Instances.doubleInstance(v);
        }

    },
    FLOAT(11, "float", float.class, FloatValue.class, TypeCategory.FLOAT) {
        @Override
        public Value getDefaultValue() {
            return Instances.floatInstance(0);
        }

        @NativeApi
        public static Value compareTo(Value self, Value that) {
            var s1 = (FloatValue) self;
            var s2 = (FloatValue) that;
            return Instances.intInstance(s1.compareTo(s2));
        }

        @NativeApi
        public static Value intValue(Value self) {
            var v = ((FloatValue) self).value;
            return Instances.intInstance((int) v);
        }

        @NativeApi
        public static Value longValue(Value self) {
            var v = ((FloatValue) self).value;
            return Instances.longInstance((long) v);
        }

        @NativeApi
        public static Value floatValue(Value self) {
            return self;
        }

        @NativeApi
        public static Value doubleValue(Value self) {
            var v = ((FloatValue) self).value;
            return Instances.doubleInstance(v);
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
    private final String name;
    private final Class<?> javaClass;
    private final Class<? extends Value> instanceClass;
    private final TypeCategory typeCategory;
    private final String typeCode;
    private PrimitiveType type;
    private transient ValueHolder<Klass> valueHolder = new HybridValueHolder<>();

    PrimitiveKind(int code, String name, Class<?> javaClass, Class<? extends Value> instanceClass, TypeCategory typeCategory) {
        this.code = code;
        this.name = name;
        this.javaClass = javaClass;
        this.instanceClass = instanceClass;
        this.typeCategory = typeCategory;
        this.typeCode = NamingUtils.firstCharToUpperCase(this.name().toLowerCase());
    }

    public String getName() {
        return name;
    }

    public boolean checkValue(Object value) {
        return value != null && value.getClass() == javaClass;
    }

    public Class<?> getJavaClass() {
        return javaClass;
    }

    public Class<? extends Value> getInstanceClass() {
        return instanceClass;
    }

    public TypeCategory getTypeCategory() {
        return typeCategory;
    }

    public static PrimitiveKind fromJavaClass(Class<?> javaClass) {
        return Utils.findRequired(values(), v -> v.javaClass == javaClass);
    }

    public String getTypeCode() {
        return typeCode;
    }

    public int code() {
        return code;
    }

    public static PrimitiveKind fromCode(int code) {
        return Utils.findRequired(values(), v -> v.code == code);
    }

    public PrimitiveType getType() {
        return type;
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

    @Override
    public ValueHolder<Klass> getValueHolder() {
        return valueHolder;
    }

    @Override
    public void setValueHolder(ValueHolder<Klass> valueHolder) {
        this.valueHolder = valueHolder;
    }

}
