package org.metavm.util;

import org.metavm.wire.DefaultWireInput;
import org.metavm.entity.Entity;
import org.metavm.entity.StdKlass;
import org.metavm.flow.FunctionRef;
import org.metavm.flow.LambdaRef;
import org.metavm.flow.MethodRef;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.instance.core.*;
import org.metavm.object.type.*;

import java.io.Closeable;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public abstract class MvInput extends DefaultWireInput implements Closeable {

    public MvInput(InputStream in) {
        super(in);
    }

    public Type readType() {
        return (Type) readValue();
    }

    public String readUTF() {
        int len = readInt();
        byte[] bytes = new byte[len];
        read(bytes);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    public Id readId() {
        return Id.readId(this);
    }

    public Value readValue() {
        return readValue(read());
    }

    private Value readValue(int wireType) {
        return switch (wireType) {
            case WireTypes.NULL -> new NullValue();
            case WireTypes.DOUBLE -> new DoubleValue(readDouble());
            case WireTypes.FLOAT -> new FloatValue(readFloat());
            case WireTypes.STRING -> Instances.stringInstance(readUTF());
            case WireTypes.LONG -> new LongValue(readLong());
            case WireTypes.INT -> new IntValue(readInt());
            case WireTypes.CHAR -> new CharValue(readChar());
            case WireTypes.SHORT -> new ShortValue((short) readShort());
            case WireTypes.BYTE -> new ByteValue((byte) read());
            case WireTypes.BOOLEAN -> new BooleanValue(readBoolean());
            case WireTypes.TIME -> new TimeValue(readLong());
            case WireTypes.PASSWORD -> new PasswordValue(readUTF());
            case WireTypes.REFERENCE -> readReference();
            case WireTypes.INSTANCE -> readInstance();
            case WireTypes.VALUE_INSTANCE -> readValueInstance();
            case WireTypes.REMOVING_INSTANCE ->  readRemovingInstance();
            case WireTypes.CLASS_TYPE -> KlassType.read(this);
            case WireTypes.PARAMETERIZED_TYPE -> KlassType.readParameterized(this);
            case WireTypes.VARIABLE_TYPE -> VariableType.read(this);
            case WireTypes.CAPTURED_TYPE -> CapturedType.read(this);
            case WireTypes.LONG_TYPE -> PrimitiveType.longType;
            case WireTypes.INT_TYPE -> PrimitiveType.intType;
            case WireTypes.CHAR_TYPE -> PrimitiveType.charType;
            case WireTypes.SHORT_TYPE -> PrimitiveType.shortType;
            case WireTypes.BYTE_TYPE -> PrimitiveType.byteType;
            case WireTypes.DOUBLE_TYPE -> PrimitiveType.doubleType;
            case WireTypes.FLOAT_TYPE -> PrimitiveType.floatType;
            case WireTypes.NULL_TYPE -> NullType.instance;
            case WireTypes.VOID_TYPE -> PrimitiveType.voidType;
            case WireTypes.TIME_TYPE -> PrimitiveType.timeType;
            case WireTypes.PASSWORD_TYPE -> PrimitiveType.passwordType;
            case WireTypes.STRING_TYPE -> StdKlass.string.type();
            case WireTypes.BOOLEAN_TYPE -> PrimitiveType.booleanType;
            case WireTypes.FUNCTION_TYPE -> FunctionType.read(this);
            case WireTypes.UNCERTAIN_TYPE -> UncertainType.read(this);
            case WireTypes.UNION_TYPE -> UnionType.read(this);
            case WireTypes.INTERSECTION_TYPE -> IntersectionType.read(this);
            case WireTypes.READ_ONLY_ARRAY_TYPE -> ArrayType.read(this, ArrayKind.READ_ONLY);
            case WireTypes.ARRAY_TYPE -> ArrayType.read(this, ArrayKind.DEFAULT);
            case WireTypes.NEVER_TYPE -> NeverType.instance;
            case WireTypes.ANY_TYPE -> AnyType.instance;
            case WireTypes.FIELD_REF -> FieldRef.read(this);
            case WireTypes.METHOD_REF -> MethodRef.read(this);
            case WireTypes.FUNCTION_REF -> FunctionRef.read(this);
            case WireTypes.INDEX_REF -> IndexRef.read(this);
            case WireTypes.LAMBDA_REF -> LambdaRef.read(this);
            default -> throw new IllegalStateException("Invalid wire type: " + wireType);
        };
    }

    public abstract Message readTree();

    public abstract Value readRemovingInstance();

    public abstract Value readValueInstance();

    public abstract Value readInstance();

    public abstract Reference readReference();

    public abstract Entity readEntityMessage();

}
