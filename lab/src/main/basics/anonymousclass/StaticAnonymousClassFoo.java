package anonymousclass;

import java.util.Iterator;

public class StaticAnonymousClassFoo {

    public static Iterator<String> iterator() {
        return new Iterator<>() {
            @Override
            public boolean hasNext() {
                return false;
            }

            @Override
            public String next() {
                throw new IllegalStateException();
            }
        };
    }

    public static boolean test() {
        return iterator().hasNext();
    }

}
