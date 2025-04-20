package wildcard_capture;

public class LocalClassTypeCapture {

    public static Object test(Object value) {
        record Foo<T>(T value) {
        }

        class Util {

            public static Object getValue(Foo<?> foo) {
                return getValueHelper(foo);
            }

            public static <T> T getValueHelper(Foo<T> foo) {
                return foo.value;
            }

        }

        return Util.getValue(new Foo<>(value));
    }

}
