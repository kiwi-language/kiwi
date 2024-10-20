package fieldinitializer;

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

    public MoveFieldInitializerFoo(Object value) {
        value.hashCode();
    }

    public int getValue1() {
        return value1;
    }

}