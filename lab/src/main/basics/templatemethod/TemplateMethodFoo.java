package templatemethod;

import java.util.Comparator;

public class TemplateMethodFoo<T> {

    public static <T> Comparator<T> getComparator() {
//        noinspection ComparatorCombinators
        return (t1, t2) -> t1.toString().compareTo(t2.toString());
    }

    public static int compare(String s1, String s2) {
        return getComparator().compare(s1, s2);
    }

}
