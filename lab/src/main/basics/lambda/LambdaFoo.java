package lambda;

import org.jetbrains.annotations.NotNull;

import java.util.Comparator;

public class LambdaFoo {

    public static <T extends Comparable<? super T>> Comparator<T> comparator() {
        //noinspection ComparatorCombinators
        return (t1, t2) -> t1.compareTo(t2);
    }

    public static int compare(long v1, long v2) {
        Comparator<Item> cmp = comparator();
        return cmp.compare(new Item(v1), new Item(v2));
    }

    private record Item(long value) implements Comparable<Item> {

        @Override
        public int compareTo(@NotNull LambdaFoo.Item o) {
            return Long.compare(value, o.value);
        }

    }

}
