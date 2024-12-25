package org.metavm.util;

import org.metavm.flow.*;
import org.metavm.object.instance.core.Value;
import org.metavm.object.instance.core.*;
import org.metavm.object.type.*;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public abstract class MvInput implements Closeable {

    private final InputStream in;

    public MvInput(InputStream in) {
        this.in = in;
    }

    public Type readType() {
        return (Type) readValue();
    }

    public int read(byte[] buf) {
        try {
            int offset = 0;
            int len = buf.length;
            while (offset < len) {
                offset += in.read(buf, offset, len - offset);
            }
            return len;
        } catch (IOException e) {
            throw new InternalException("Failed to read from the underlying input steam", e);
        }
    }

    public int read() {
        try {
            return in.read();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void skip(int len) {
        try {
            //noinspection ResultOfMethodCallIgnored
            in.skip(len);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() throws IOException {
        in.close();
    }

    public InputStream getIn() {
        return in;
    }

    public String readUTF() {
        int len = readInt();
        byte[] bytes = new byte[len];
        read(bytes);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    public int readShort() {
        return read() << 8 | read();
    }

    public int readInt() {
        return (int) readLong();
    }

    public long readLong() {
        int b = read();
        boolean negative = (b & 1) == 1;
        long v = b >> 1 & 0x3f;
        int shifts = 6;
        for (int i = 0; (b & 0x80) != 0 && i < 10; i++, shifts += 7) {
            b = read();
            v |= (long) (b & 0x7f) << shifts;
        }
        return negative ? -v : v;
    }

    public float readFloat() {
        return Float.intBitsToFloat(readInt());
    }

    public TypeTag readTypeTag() {
        return TypeTag.fromCode(read());
    }

    public double readDouble() {
        long l = 0;
        for (int shifts = 0; shifts < 64; shifts += 8)
            l |= (long) read() << shifts;
        return Double.longBitsToDouble(l);
    }

    public boolean readBoolean() {
        return read() != 0;
    }

    public char readChar() {
        int firstByte = read();
        if (firstByte == -1)
            throw new InternalException("End of stream reached");
        if ((firstByte & 0x80) == 0) {
            return (char) firstByte;
        } else if ((firstByte & 0xE0) == 0xC0) {
            int secondByte = read();
            return (char) (((firstByte & 0x1F) << 6) | (secondByte & 0x3F));
        } else if ((firstByte & 0xF0) == 0xE0) {
            int secondByte = read();
            int thirdByte = read();
            return (char) (((firstByte & 0x0F) << 12) | ((secondByte & 0x3F) << 6) | (thirdByte & 0x3F));
        } else
            throw new InternalException("Invalid UTF-8 byte sequence");
    }

    public Id readId() {
        return Id.readId(this);
    }

    public <T> List<T> readList(Supplier<T> read) {
        var size = readInt();
        var list = new ArrayList<T>(size);
        for (int i = 0; i < size; i++) {
            list.add(read.get());
        }
        return list;
    }

    public Value readValue() {
        var wireType = read();
        return switch (wireType) {
            case WireTypes.NULL -> new NullValue();
            case WireTypes.DOUBLE -> new DoubleValue(readDouble());
            case WireTypes.FLOAT -> new FloatValue(readFloat());
            case WireTypes.STRING -> new StringValue(readUTF());
            case WireTypes.LONG -> new LongValue(readLong());
            case WireTypes.INT -> new IntValue(readInt());
            case WireTypes.CHAR -> new CharValue(readChar());
            case WireTypes.SHORT -> new ShortValue((short) readShort());
            case WireTypes.BYTE -> new ByteValue((byte) read());
            case WireTypes.BOOLEAN -> new BooleanValue(readBoolean());
            case WireTypes.TIME -> new TimeValue(readLong());
            case WireTypes.PASSWORD -> new PasswordValue(readUTF());
            case WireTypes.FLAGGED_REFERENCE -> readFlaggedReference();
            case WireTypes.REFERENCE -> readReference();
            case WireTypes.REDIRECTING_REFERENCE -> readRedirectingReference();
            case WireTypes.REDIRECTING_INSTANCE -> readRedirectingInstance();
            case WireTypes.INSTANCE -> readInstance();
            case WireTypes.RELOCATING_INSTANCE -> readRelocatingInstance();
            case WireTypes.VALUE_INSTANCE -> readValueInstance();
            case WireTypes.REMOVING_INSTANCE ->  readRemovingInstance();
            case WireTypes.CLASS_TYPE -> KlassType.read(this);
            case WireTypes.TAGGED_CLASS_TYPE -> KlassType.readTagged(this);
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
            case WireTypes.STRING_TYPE -> PrimitiveType.stringType;
            case WireTypes.BOOLEAN_TYPE -> PrimitiveType.booleanType;
            case WireTypes.FUNCTION_TYPE -> FunctionType.read(this);
            case WireTypes.UNCERTAIN_TYPE -> UncertainType.read(this);
            case WireTypes.UNION_TYPE -> UnionType.read(this);
            case WireTypes.INTERSECTION_TYPE -> IntersectionType.read(this);
            case WireTypes.READ_ONLY_ARRAY_TYPE -> ArrayType.read(this, ArrayKind.READ_ONLY);
            case WireTypes.READ_WRITE_ARRAY_TYPE -> ArrayType.read(this, ArrayKind.READ_WRITE);
            case WireTypes.CHILD_ARRAY_TYPE -> ArrayType.read(this, ArrayKind.CHILD);
            case WireTypes.VALUE_ARRAY_TYPE -> ArrayType.read(this, ArrayKind.VALUE);
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

    public abstract Value readRemovingInstance();

    public abstract Value readValueInstance();

    public abstract Value readRelocatingInstance();

    public abstract Value readInstance();

    public abstract Value readRedirectingInstance();

    public abstract Value readRedirectingReference();

    public abstract Value readReference();

    public abstract Value readFlaggedReference();

    public abstract Klass getKlass(Id id);

    public abstract Method getMethod(Id id);

    public abstract Field getField(Id id);

    public abstract TypeVariable getTypeVariable(Id id);

    public abstract Function getFunction(Id id);

    public abstract CapturedTypeVariable getCapturedTypeVariable(Id id);

    public abstract Lambda getLambda(Id id);

    public abstract Index getIndex(Id id);

    public abstract IndexField getIndexField(Id id);

}
