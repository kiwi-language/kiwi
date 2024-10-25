package conditional;

public class BooleanConditionalFoo {

    public static boolean test(int v) {
        return v > 0 ? v == 10 : -v == 10;
    }

}
