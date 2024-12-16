package org.metavm.object.type;

import lombok.extern.slf4j.Slf4j;
import org.metavm.entity.DefContext;
import org.metavm.entity.natives.CallContext;
import org.metavm.entity.natives.HybridValueHolder;
import org.metavm.entity.natives.ValueHolder;
import org.metavm.entity.natives.ValueHolderOwner;
import org.metavm.flow.Method;
import org.metavm.flow.MethodBuilder;
import org.metavm.flow.MethodRef;
import org.metavm.flow.NameAndType;
import org.metavm.object.instance.core.*;
import org.metavm.util.*;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Slf4j
public enum PrimitiveKind implements ValueHolderOwner<Klass> {
    LONG(1, "long", long.class, LongValue.class, TypeCategory.LONG) {
        @Override
        public Value getDefaultValue() {
            return Instances.longInstance(0L);
        }

        public static Value compareTo(Value v1, Value v2, CallContext callContext) {
            var s1 = (LongValue) v1;
            var s2 = (LongValue) v2;
            return Instances.intInstance(s1.compareTo(s2));
        }

    },
    DOUBLE(2, "double", double.class, DoubleValue.class, TypeCategory.DOUBLE) {
        @Override
        public Value getDefaultValue() {
            return Instances.doubleInstance(0.0);
        }

        public static Value compareTo(Value v1, Value v2, CallContext callContext) {
            var s1 = (DoubleValue) v1;
            var s2 = (DoubleValue) v2;
            return Instances.intInstance(s1.compareTo(s2));
        }

    },
    STRING(3, "string", String.class, StringValue.class, TypeCategory.STRING) {

        public static Value compareTo(Value v1, Value v2, CallContext callContext) {
            var s1 = (StringValue) v1;
            var s2 = (StringValue) v2;
            return Instances.intInstance(s1.compareTo(s2));
        }

        public static Value length(Value v, CallContext callContext) {
            var s = (StringValue) v;
            return Instances.intInstance(s.value.length());
        }

        public static Value charAt(Value v1, Value v2, CallContext callContext) {
            var s = (StringValue) v1;
            var idx = ((IntValue) v2).value;
            return Instances.intInstance(s.value.charAt(idx));
        }

        public static Value subSequence(Value s, Value start, Value end, CallContext callContext) {
            var s1 = ((StringValue) s).value;
            var i1 = ((IntValue) start).value;
            var i2 = ((IntValue) end).value;
            return Instances.stringInstance(s1.substring(i1, i2));
        }

        public static Value toString(Value v, CallContext callContext) {
            return v;
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

        public static Value compareTo(Value v1, Value v2, CallContext callContext) {
            var s1 = (BooleanValue) v1;
            var s2 = (BooleanValue) v2;
            return Instances.intInstance(s1.compareTo(s2));
        }

    },
    TIME(5, "time", Date.class, TimeValue.class, TypeCategory.TIME) {

        public static Value compareTo(Value v1, Value v2, CallContext callContext) {
            var s1 = (TimeValue) v1;
            var s2 = (TimeValue) v2;
            return Instances.intInstance(s1.compareTo(s2));
        }

    },
    PASSWORD(6, "password", Password.class, PasswordValue.class, TypeCategory.PASSWORD),
    NULL(7, "null", Null.class, NullValue.class, TypeCategory.NULL) {
        @Override
        public Value getDefaultValue() {
            return Instances.nullInstance();
        }

    },
    VOID(8, "void", void.class, null, TypeCategory.VOID),
    CHAR(9, "char", char.class, CharValue.class, TypeCategory.CHAR) {
        @Override
        public Value fromStackValue(Value value) {
            var i = ((IntValue) value).value;
            return new CharValue((char) i);
        }

        public static Value compareTo(Value v1, Value v2, CallContext callContext) {
            var s1 = (CharValue) v1;
            var s2 = (CharValue) v2;
            return Instances.intInstance(s1.compareTo(s2));
        }

    },
    INT(10, "int", int.class, IntValue.class, TypeCategory.INT) {
        @Override
        public Value getDefaultValue() {
            return Instances.intInstance(0);
        }

        public static Value compareTo(Value v1, Value v2, CallContext callContext) {
            var s1 = (IntValue) v1;
            var s2 = (IntValue) v2;
            return Instances.intInstance(s1.compareTo(s2));
        }

    },
    FLOAT(11, "float", float.class, FloatValue.class, TypeCategory.FLOAT) {
        @Override
        public Value getDefaultValue() {
            return Instances.floatInstance(0);
        }

        public static Value compareTo(Value v1, Value v2, CallContext callContext) {
            var s1 = (FloatValue) v1;
            var s2 = (FloatValue) v2;
            return Instances.intInstance(s1.compareTo(s2));
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

        public static Value compareTo(Value v1, Value v2, CallContext callContext) {
            var s1 = (ShortValue) v1;
            var s2 = (ShortValue) v2;
            return Instances.intInstance(s1.compareTo(s2));
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

        public static Value compareTo(Value v1, Value v2, CallContext callContext) {
            var s1 = (ByteValue) v1;
            var s2 = (ByteValue) v2;
            return Instances.intInstance(s1.compareTo(s2));
        }

    };


    private final int code;
    private final String name;
    private final Class<?> javaClass;
    private final Class<? extends Value> instanceClass;
    private final TypeCategory typeCategory;
    private final String typeCode;
    private PrimitiveType type;
    private ValueHolder<Klass> valueHolder = new HybridValueHolder<>();

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
        return NncUtils.findRequired(values(), v -> v.javaClass == javaClass);
    }

    public String getTypeCode() {
        return typeCode;
    }

    public int code() {
        return code;
    }

    public static PrimitiveKind fromCode(int code) {
        return NncUtils.findRequired(values(), v -> v.code == code);
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

    public Klass getKlass() {
        var klass = valueHolder.get();
        if (klass == null) {
            klass = KlassBuilder.newBuilder(getName(), getName()).build();
            klass.setType(Objects.requireNonNull(type));
            valueHolder.set(klass);
        }
        return klass;
    }

    public static void initialize(DefContext defContext) {
        for (PrimitiveKind kind : PrimitiveKind.values()) {
            initPrimitiveKlass(kind.getType(), defContext);
        }
    }

    private static void initPrimitiveKlass(PrimitiveType primitiveType, DefContext defContext) {
        var klass = primitiveType.getKlass();
        var interfaces = new ArrayList<ClassType>();
        if (primitiveType != PrimitiveType.voidType && primitiveType != PrimitiveType.nullType
                && primitiveType != PrimitiveType.passwordType && primitiveType != PrimitiveType.charType) {
            interfaces.add(KlassType.create(defContext.getKlass(Comparable.class), List.of(klass.getType())));
        }
        if (primitiveType == PrimitiveType.stringType)
            interfaces.add(defContext.getKlass(CharSequence.class).getType());
        klass.setInterfaces(interfaces);
        interfaces.forEach(it -> definePrimitiveMethods(primitiveType, it));
        klass.resetHierarchy();
    }

    private static void definePrimitiveMethods(PrimitiveType primitiveType, ClassType interfaceType) {
        interfaceType.foreachMethod(m -> {
            if(m.isAbstract())
                definePrimitiveMethod(primitiveType, m);
        });
    }

    private static void definePrimitiveMethod(PrimitiveType primitiveType, MethodRef interfaceMethod) {
        var method = MethodBuilder.newBuilder(primitiveType.getKlass(), interfaceMethod.getName())
                .parameters(
                        NncUtils.map(
                                interfaceMethod.getParameters(),
                                p -> new NameAndType(p.getName(), p.getType())
                        )
                )
                .returnType(interfaceMethod.getReturnType())
                .isNative(true)
                .build();
        setPrimitiveNativeMethod(primitiveType, method);
    }

    private static void setPrimitiveNativeMethod(PrimitiveType primitiveType, Method method) {
        var kind = primitiveType.getKind();
        var paramClasses = new ArrayList<Class<?>>(NncUtils.multipleOf(Value.class, 1 + method.getParameters().size()));
        paramClasses.add(CallContext.class);
        var nativeMethod = ReflectionUtils.getMethod(kind.getClass(), method.getName(), paramClasses);
        method.setNativeMethod(nativeMethod);
    }


}
