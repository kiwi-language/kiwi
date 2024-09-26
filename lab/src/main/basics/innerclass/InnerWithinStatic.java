package innerclass;

import java.util.Iterator;
import java.util.NoSuchElementException;

public abstract class InnerWithinStatic<E> {

    public abstract Iterator<E> iterator();

    private static class SubList<E> extends InnerWithinStatic<E> {

        @Override
        public Iterator<E> iterator() {
            return new MyIterator();
        }

        private class MyIterator implements Iterator<E> {

            @Override
            public boolean hasNext() {
                return false;
            }

            @Override
            public E next() {
                throw new NoSuchElementException();
            }
        }
    }

    public static boolean test() {
        return new SubList<>().iterator().hasNext();
    }

}
