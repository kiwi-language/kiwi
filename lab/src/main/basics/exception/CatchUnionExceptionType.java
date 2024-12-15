package exception;

public class CatchUnionExceptionType {

    public static final int[] array = new int[] {1, 2, 3};

    public static int get(int index) {
        try {
            return array[index];
        } catch (IndexOutOfBoundsException | NullPointerException e) {
            return -1;
        }
    }

}
