package override;

public class OverrideFoo {

    public static String test(String s) {
        Base<String> b = new Sub<>();
        return b.speak(s);
    }

    static abstract class Base<T> {

        public abstract T speak(T t);

    }

    static class Sub<T> extends Base<T> {

        public T speak(T t) {
            return t;
        }

    }

}
