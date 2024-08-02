package org.metavm.util;

import org.metavm.object.instance.core.DurableInstance;
import org.metavm.object.instance.core.Id;

import java.io.InputStream;
import java.util.function.Function;

public class IndexKeyReader extends InstanceInput {
    public IndexKeyReader(InputStream inputStream, Function<Id, DurableInstance> getInstance) {
        super(inputStream, getInstance,
                inst -> {throw new UnsupportedOperationException();},
                id -> {throw new UnsupportedOperationException();},
                id -> {throw new UnsupportedOperationException();}
        );
    }

    @Override
    public long readLong() {
        var l = readFixedLongBE();
        if((l & 0x8000000000000000L) != 0)
            l &= 0x7fffffffffffffffL;
        else
            l = ~l;
        return l;
    }

    @Override
    public double readDouble() {
        long l = readFixedLongBE();
        if((l & 0x8000000000000000L) != 0)
            l &= 0x7fffffffffffffffL;
        else
            l = ~l;
        return Double.longBitsToDouble(l);
    }

    private long readFixedLongBE() {
        long l = 0L;
        for (int i = 7; i >= 0; i--)
            l |= (long) read() << (i << 3);
        return l;
    }

}
