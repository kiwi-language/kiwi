package anonymous_class;

import java.util.Iterator;

public class AnonymousClassWithField {

    public static Iterator<String> iterator(String s) {
        return new Iterator<>() {

            private final String value = s;
            private boolean read = false;

            @Override
            public boolean hasNext() {
                return !read;
            }

            @Override
            public String next() {
                if (read)
                    throw new IllegalStateException();
                else
                    read = true;
                return value;
            }
        };
    }

    public static String test(String s) {
        var it = iterator(s);
        return it.hasNext() ? it.next() : null;
    }

}
