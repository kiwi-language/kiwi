package arrayinitializer;

public class ArrayInitializerFoo {

    private final static Object[][] array = {
            {"MetaVM"},
            {6, 6, 6}
    };

    public static final Object[] array2 = new Object[] {1, 2, 3};

    public static boolean test() {
        int sum = 0;
        for (int i = 0; i < array[1].length; i++) {
            sum += (int) array[1][i];
        }
        for (Object o : array2) {
            sum += (int) o;
        }
        return sum == 24;
    }

}
