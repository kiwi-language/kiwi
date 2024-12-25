package wildcard_capture;

public class InnerClassTypeCapture {

    public record Inner<T>(T value) {
    }

    public static <T>  T getValue(Inner<? extends T> inner) {
        return inner.value();
    }

    public static Object test(Object value) {
        return getValue(new Inner<>(value));
    }

}
