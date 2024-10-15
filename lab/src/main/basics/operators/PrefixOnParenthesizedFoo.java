package operators;

import java.util.Spliterator;

public class PrefixOnParenthesizedFoo {

    public static long test(long characteristics) {
        return characteristics & ~(Spliterator.SIZED | Spliterator.SUBSIZED);
    }

}
