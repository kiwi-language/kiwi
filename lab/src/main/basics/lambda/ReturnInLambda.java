package lambda;

import java.util.Comparator;

public class ReturnInLambda {

    public static Comparator<String> getComparator() {
        //noinspection ComparatorCombinators
        return (s1, s2) -> {
            //noinspection Convert2MethodRef
            return s1.compareTo(s2);
        };
    }

    public static int test(String s1, String s2) {
        return getComparator().compare(s1, s2);
    }

}
