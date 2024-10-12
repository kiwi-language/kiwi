package assignment;

public class UnaryAndPrefixFoo {

    public static int value;

    public static int getAndIncrement() {
        return value++;
    }

    public static int incrementAndGet() {
        return ++value;
    }

    public static int getAndDecrement() {
        return value--;
    }

    public static int decrementAndGet() {
        return --value;
    }

}
