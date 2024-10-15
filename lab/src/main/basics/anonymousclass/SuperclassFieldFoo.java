package anonymousclass;

import java.util.Iterator;

class Base {
    int value;
}

public class SuperclassFieldFoo extends Base {

    public int getValue() {

        return new Iterator<String>() {

            private int v = value;

            @Override
            public boolean hasNext() {
                return false;
            }

            @Override
            public String next() {
                return null;
            }
        }.v;
    }

    public static int test() {
        return new SuperclassFieldFoo().getValue();
    }

}
