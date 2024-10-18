package innerclass;

public class MultiLevelInnerFoo {

    class External {

        private final int value;

        External(int value) {
            this.value = value;
        }

        class Inner {

            public int getValue() {
                return External.this.value;
            }

        }

    }

    class Sub extends External {

        Sub(int value) {
            super(value);
        }

        Inner getInner() {
            return new Inner();
        }

    }

    public static int test(int value) {
        return new MultiLevelInnerFoo().new Sub(value).getInner().getValue();
    }

}
