package tech.metavm;

import com.google.common.primitives.UnsignedBytes;

import java.io.IOException;

public class Lab {

    public static final byte[] MIN_ID = new byte[0];
    public static final byte[] MAX_ID = new byte[]{
            -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1
    };

    public static final byte[] ID = {
            1, 2, 3
    };

    public static void main(String[] args) throws IOException {
        System.out.println(UnsignedBytes.lexicographicalComparator().compare(MIN_ID, MAX_ID));
        System.out.println(UnsignedBytes.lexicographicalComparator().compare(MIN_ID, ID));
        System.out.println(UnsignedBytes.lexicographicalComparator().compare(ID, MAX_ID));
    }

}
