package org.manul.wire;


import java.util.Date;
import java.util.List;
import java.util.function.Consumer;

public interface WireOutput {

    void write(int b);

    void write(byte[] b);

    void write(byte[] bytes, int offset, int length);

    void writeBoolean(boolean b);

    void writeChar(char c);

    void writeByte(byte v);

    void writeShort(int v);

    void writeInt(int v);

    void writeFixedInt(int v);

    void writeLong(long v);

    void writeFloat(float v);

    void writeDouble(double v);

    void writeString(String s);

    void writeDate(Date date);

    void writeBytes(byte[] b);

    <T> void writeEntity(T o, WireAdapter<T> adapter);

    <T> void writeNullable(T o, Consumer<? super T> write);

    <E> void writeList(List<E> list, Consumer<? super E> write);

    <E> void writeArray(E[] array, Consumer<? super E> write);

}
