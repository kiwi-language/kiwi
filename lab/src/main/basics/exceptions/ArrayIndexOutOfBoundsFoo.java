package exceptions;

public class ArrayIndexOutOfBoundsFoo {

    public static void test(int index) {
        throw new ArrayIndexOutOfBoundsException(index);
    }

}
