package anonymous_class;

public class AnonymousClassWithArgs {
    
    public static int test(int value) {
        return new Base(value) {}.getValue();
    }
    
    public static abstract class Base {
        private final int value;

        protected Base(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }
    
}
