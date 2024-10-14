package localclass;

import java.util.Iterator;
import java.util.List;

public class LocalClassFoo {

    public static <T> Iterator<T> adapt(Iterator<? extends T> iterator) {
        class Adapter implements Iterator<T> {

            private boolean empty = !iterator.hasNext();
            private int index;

            Adapter() {
                this(0);
            }

            Adapter(int index) {
                super();
                this.index = index;
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
        if(sequences.isEmpty())
            return "";
        var it = adapt(sequences.iterator());
        var sb = new StringBuilder(it.next());
        while (it.hasNext()) {
            sb.append(' ');
            sb.append(it.next());
        }
        return sb.toString();
    }

}
