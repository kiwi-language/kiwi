package innerclass;

public class InnerClassExtension {

    private final int value0;

    public InnerClassExtension(int value0) {
        this.value0 = value0;
    }

    class Inner2 extends Inner1 {

        private final int value3;

        Inner2(int value1, int value3) {
            super(value1);
            this.value3 = value3;
        }

        class Inner21 extends Inner11 {

            private final int value4;

            Inner21(int value2, int value4) {
                super(value2);
                this.value4 = value4;
            }

            public int sum() {
                return value0 + value1 + value2 + value3 + value4;
            }

            public int value1() {
                return value1;
            }
        }

    }

    class Inner1 {

        final int value1;

        Inner1(int value1) {
            this.value1 = value1;
        }

        class Inner11 {

            final int value2;

            Inner11(int value2) {
                this.value2 = value2;
            }
        }

    }

    public static int sum(int v1, int v2, int v3, int v4) {
        var i1 = new InnerClassExtension(0).new Inner2(v1, v3);
        var i2 = i1.new Inner21(v2, v4);
        return i2.sum();
    }

}
