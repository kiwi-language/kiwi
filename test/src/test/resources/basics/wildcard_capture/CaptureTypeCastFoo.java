package wildcard_capture;

import java.util.List;
import java.util.Objects;

public class CaptureTypeCastFoo {

    public static boolean listEquals(Object o1, Object o2) {
        var it1 = ((List<?>) o1).iterator();
        var it2 = ((List<?>) o2).iterator();
        while (it1.hasNext() && it2.hasNext()) {
            if(!Objects.equals(it1.next(), it2.next()))
                return false;
        }
        return !it1.hasNext() && !it2.hasNext();
    }

}
