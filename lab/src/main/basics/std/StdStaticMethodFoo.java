package std;

import java.util.Comparator;
import java.util.Map;

public class StdStaticMethodFoo {

    public static Comparator<Map.Entry<Object, Object>> test() {
        return Map.Entry.comparingByKey(Comparator.comparingInt(o -> -o.hashCode()));
    }

}
