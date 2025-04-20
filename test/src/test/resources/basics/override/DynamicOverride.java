package override;

public class DynamicOverride {

    public static boolean test() {
        It2 it = new Sub();
        return it.hasNext();
    }

    interface It1 {

        boolean hasNext();

    }

    interface It2 {

        boolean hasNext();

    }

    static class Base implements It1 {

        @Override
        public boolean hasNext() {
            return true;
        }
    }

    static class Sub extends Base implements It2 {}


}
