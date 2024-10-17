package innerclass;

public class InnerCallsExternal {

    private final int value;

    public InnerCallsExternal(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    class Inner {

        private final int value;

        public Inner() {
            this.value = getValue();
        }
    }

    public static int test(int value) {
        var o = new InnerCallsExternal(value);
        return o.new Inner().value;
    }

}
