package fieldinitializer;

import java.util.function.IntSupplier;

public class MoveFieldInitializerFoo {

    public static int defaultValue = 0;

    private int value = defaultValue;
    private int value1 = value + 1;

    public MoveFieldInitializerFoo() {
        this(0, 0);
    }

    public MoveFieldInitializerFoo(Object value, Object defaultValue) {
        super();
    }

    public int getValue1() {
        return value1;
    }

    public IntSupplier getSupplier() {
        return new IntSupplier() {

            private final int v = value;

            @Override
            public int getAsInt() {
                return v;
            }
        };
    }

    class Inner {
        private final int v = value;
        private final int v1 = getValue1();

        public Inner(Object value) {
        }
    }
}