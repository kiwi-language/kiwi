package capturedtypes;

import org.metavm.api.EntityType;

import java.util.Comparator;

@EntityType(ephemeral = true)
public class CapturedTypesInFieldInitializer<K> {

    private final Comparator<? super K> comparator = (k1,k2) -> k2.toString().compareTo(k1.toString());

    private final Comparator<? super K> reverseComparator = reverseOrder(comparator);

    private static <T> Comparator<T> reverseOrder(Comparator<T> cmp) {
        return (t1, t2) -> cmp.compare(t2, t1);
    }

    public static int test(String s1, String s2) {
        return new CapturedTypesInFieldInitializer<String>().reverseComparator.compare(s1, s2);
    }

}
