package org.metavm.wire;

import lombok.SneakyThrows;

import javax.annotation.Nullable;
import java.util.Date;
import java.util.List;
import java.util.function.IntFunction;
import java.util.function.Supplier;

public interface WireInput {

    byte readByte();

    short readShort();

    int readInt();

    int readFixedInt();

    long readLong();

    float readFloat();

    double readDouble();

    char readChar();

    boolean readBoolean();

    String readString();

    Date readDate();

    byte[] readBytes();

    <T> T readEntity(WireAdapter<T> adapter, @Nullable Object parent);

    Object readEntity();

    Object readEntity(Object parent);

    <T> T readNullable(Supplier<T> read);

    <T> List<T> readList(Supplier<T> readElement);

    <T> T[] readArray(Supplier<T> readElement, IntFunction<T[]> generator);

    @SneakyThrows
    int read();
}
