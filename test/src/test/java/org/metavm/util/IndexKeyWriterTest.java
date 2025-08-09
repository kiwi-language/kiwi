package org.metavm.util;

import com.google.common.primitives.UnsignedBytes;
import junit.framework.TestCase;
import org.jetbrains.annotations.NotNull;
import org.junit.Assert;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

public class IndexKeyWriterTest extends TestCase {

    public void testWriteSortedDouble() {
        var values = new ArrayList<>(List.of(
                DoubleValue.create(-1000.0),
                DoubleValue.create(-0.1), DoubleValue.create(-0.0), DoubleValue.create(1.0),
                DoubleValue.create(1000.0)
        ));
        Collections.sort(values);
        Assert.assertEquals(-1000, values.getFirst().value, 0.0);
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
        Assert.assertEquals(-1L, values.getFirst().value);
        Assert.assertEquals(0L, values.get(1).value);
        Assert.assertEquals(1L, values.get(2).value);
        Assert.assertEquals(2L, values.get(3).value);
    }

    public void testNegativeInt() {
        var bout = new ByteArrayOutputStream();
        var writer = new IndexKeyWriter(bout);
        writer.writeInt(-1);

        var bout1 = new ByteArrayOutputStream();
        var writer1 = new IndexKeyWriter(bout1);
        writer1.writeInt(-2);

        var r = UnsignedBytes.lexicographicalComparator().compare(bout.toByteArray(), bout1.toByteArray());
        assertTrue(r > 0);
    }

    public void testNegativeDouble() {
        var rand = new Random();
        for (int i = 0; i < 10000; i++) {
            var bout = new ByteArrayOutputStream();
            var writer = new IndexKeyWriter(bout);
            var d1 = rand.nextDouble() * rand.nextInt();
            writer.writeDouble(d1);

            var bout1 = new ByteArrayOutputStream();
            var writer1 = new IndexKeyWriter(bout1);
            var d2 = rand.nextDouble() + rand.nextInt();
            writer1.writeDouble(d2);

            var r = UnsignedBytes.lexicographicalComparator().compare(bout.toByteArray(), bout1.toByteArray());
            assertEquals(
                    normalizeCompareResult(Double.compare(d1, d2)),
                    normalizeCompareResult(r)
            );
        }
    }

    private int normalizeCompareResult(int r) {
        return r / Math.abs(r);
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