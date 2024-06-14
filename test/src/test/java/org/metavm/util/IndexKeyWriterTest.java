package org.metavm.util;

import junit.framework.TestCase;
import org.jetbrains.annotations.NotNull;
import org.junit.Assert;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class IndexKeyWriterTest extends TestCase {

    public void testWriteSortedDouble() {
        var values = new ArrayList<>(List.of(
                DoubleValue.create(-1000.0),
                DoubleValue.create(-0.1), DoubleValue.create(-0.0), DoubleValue.create(1.0),
                DoubleValue.create(1000.0)
        ));
        Collections.sort(values);
        Assert.assertEquals(-1000, values.get(0).value, 0.0);
        Assert.assertEquals(-0.1, values.get(1).value, 0.0);
        Assert.assertEquals(0, values.get(2).value, 0.0);
        Assert.assertEquals(1.0, values.get(3).value, 0.0);
        Assert.assertEquals(1000.0, values.get(4).value, 0.0);
    }

    private record DoubleValue(
            double value,
            byte[] bytes,
            String binaryStr
    ) implements Comparable<DoubleValue> {

        public static DoubleValue create(double l) {
            var bytes = toBytes(out -> out.writeDouble(l));
            return new DoubleValue(l, bytes, BytesUtils.toBinaryString(bytes));
        }

        @Override
        public int compareTo(@NotNull IndexKeyWriterTest.DoubleValue o) {
            return BytesUtils.compareBytes(bytes, o.bytes);
        }
    }

    public void testWriteSortedLong() {
        List<LongValue> values = new ArrayList<>(List.of(
                LongValue.create(-1L), LongValue.create(0), LongValue.create(1L), LongValue.create(2L)
        ));
        Collections.sort(values);
        Assert.assertEquals(-1L, values.get(0).value);
        Assert.assertEquals(0L, values.get(1).value);
        Assert.assertEquals(1L, values.get(2).value);
        Assert.assertEquals(2L, values.get(3).value);
    }

    private record LongValue(
            long value,
            byte[] bytes,
            String binaryStr
    ) implements Comparable<LongValue> {

        public static LongValue create(long l) {
            var bytes = toBytes(out -> out.writeLong(l));
            return new LongValue(l, bytes, BytesUtils.toBinaryString(bytes));
        }

        @Override
        public int compareTo(@NotNull IndexKeyWriterTest.LongValue o) {
            return BytesUtils.compareBytes(bytes, o.bytes);
        }
    }

    private static byte[] toBytes(Consumer<IndexKeyWriter> action) {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        action.accept(new IndexKeyWriter(bout));
        return bout.toByteArray();
    }

}