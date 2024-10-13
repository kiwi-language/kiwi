package assignment;

public class FieldAssignmentFoo {

    private int value;

    public static void setValue(FieldAssignmentFoo foo, int value) {
        foo.value = value;
    }

    public int getValue() {
        return value;
    }
}
