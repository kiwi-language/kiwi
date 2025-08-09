package org.metavm.util;

import java.io.OutputStream;

public class IndexKeyWriter extends InstanceOutput {
    public IndexKeyWriter(OutputStream outputStream) {
        super(outputStream);
    }

    @Override
    public void writeLong(long l) {
        if (l >= 0)
            l |= 0x8000000000000000L;
        else
            l &= ~0x8000000000000000L;;
        writeFixedLongBE(l);
    }

    @Override
    public void writeDouble(double d) {
        long l = Double.doubleToRawLongBits(d);
        if(d >= 0.0)
            l |= 0x8000000000000000L;
        else
            l = ~l;
        writeFixedLongBE(l);
    }

    private void writeFixedLongBE(long l) {
        for (int i = 7; i >= 0; i--)
            write((int) (l >> (i << 3) & 0xff));
    }

}
