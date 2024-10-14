package localclass;

import java.util.Iterator;
import java.util.List;

public class LocalClassFoo {

    private int count;
    public static final int limit = 16;

    public <T> Iterator<T> adapt(Iterator<? extends T> iterator) {
        class Adapter implements Iterator<T> {

            private boolean empty = !iterator.hasNext();
            private int index;
            private int c = count + 1;
            private int l = limit + 1;
            private int m = l + 1;

            Adapter() {
                this(0);
            }

            Adapter(int index) {
                super();
                this.index = index;
            }

            Adapter(Object iterator, Object count, Object limit) {
            }

            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public T next() {
                return iterator.next();
            }
        }
        return new Adapter();
    }

    public static String concatenate(List<? extends String> sequences) {
        var foo = new LocalClassFoo();
        if (sequences.isEmpty())
            return "";
        var it = foo.adapt(sequences.iterator());
        var sb = new StringBuilder(it.next());
        while (it.hasNext()) {
            sb.append(' ');
            sb.append(it.next());
        }
        return sb.toString();
    }

}
