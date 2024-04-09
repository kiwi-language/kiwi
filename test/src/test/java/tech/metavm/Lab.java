package tech.metavm;

import tech.metavm.object.instance.core.DefaultViewId;
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
        var id = (DefaultViewId) Id.parse("0b01a6f3beda0dcc0203d699060001f4b4bfda0d0003a6f3beda0d00");
        System.out.println(id.getSourceId());
    }

    public void test(List<? extends Number> list) {
        helper(list);
    }

    public <T> void helper(List<T> t) {
        t.add(t.get(0));
    }

}
