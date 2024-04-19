package genericoverride;

public class Base {

    public <T> boolean containsAny(Iterable<? extends T> iterable, Iterable<? extends T> values) {
        return false;
    }

}
