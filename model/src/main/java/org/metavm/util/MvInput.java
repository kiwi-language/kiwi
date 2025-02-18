package org.metavm.util;

import org.metavm.entity.Entity;
import org.metavm.entity.StdKlass;
import org.metavm.flow.FunctionRef;
import org.metavm.flow.LambdaRef;
import org.metavm.flow.MethodRef;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.instance.core.*;
import org.metavm.object.type.*;

import javax.annotation.Nullable;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.IntFunction;
import java.util.function.Supplier;

public abstract class MvInput implements Closeable {

    private final InputStream in;

    public MvInput(InputStream in) {
        this.in = in;
    }

    public Type readType() {
        return (Type) readValue();
    }

    public byte[] readBytes() {
        var buf = new byte[readInt()];
        read(buf);
        return buf;
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

    public int readFixedInt() {
        return (read() & 0xff) << 24 | (read() & 0xff) << 16 | (read() & 0xff) << 8 | read() & 0xff;
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

    public Date readDate() {
        return new Date(readLong());
    }

    public float readFloat() {
        return Float.intBitsToFloat(readInt());
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

    public <T> T[] readArray(Supplier<T> read, IntFunction<T[]> newArray) {
        var size = readInt();
        var array = newArray.apply(size);
        for (int i = 0; i < size; i++) {
            array[i] = read.get();
        }
        return array;
    }

    public <T> @Nullable T readNullable(Supplier<T> read) {
        return readBoolean() ? read.get() : null;
    }

    public Value readValue() {
        return readValue(read());
    }

    protected Value readValue(int wireType) {
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
            case WireTypes.FLAGGED_REFERENCE -> readFlaggedReference();
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

    public <T extends Entity> T readEntity(Class<T> klass, Entity parent) {
        var entity = getEntity(klass, readId());
        entity.readHeadAndBody(this, parent);
        return entity;
    }

    public abstract Reference readReference();

    public abstract Value readFlaggedReference();

    protected <T extends Entity> T getEntity(Class<T> klass, Id id) {
        var entity = ReflectionUtils.allocateInstance(klass);
        entity.initState(id, 0, 0, false, false);
        return entity;
    }

    public abstract Entity readEntityMessage();

}
