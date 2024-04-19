package genericoverride;

public class Sub extends Base {

    @Override
    public <T> boolean containsAny(Iterable<? extends T> iterable, Iterable<? extends T> values) {
        for (T t : iterable) {
            for (T value : values) {
               if(value == t)
                   return true;
            }
        }
        return false;
    }
}
