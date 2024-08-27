package typenarrowing;

import javax.annotation.Nullable;

public class TypeNarrowingFoo {

    public static Object requireNonNull(@Nullable Object value) {
        if(value == null)
            throw new NullPointerException();
        return value;
    }

}
