package tech.metavm;

import tech.metavm.object.instance.core.Id;

import java.io.IOException;
import java.util.List;

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
        var id = Id.parse("01dad70301e0dc03");
        System.out.println(id);
    }

    public void test(List<? extends Number> list) {
        var n = list.get(0);
        n = list.get(1);
    }

}
