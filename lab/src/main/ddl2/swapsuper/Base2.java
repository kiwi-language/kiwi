package swapsuper;

public class Base2 extends Base1 {

    private final int value2;

    public Base2(int value1, int value2) {
        super(value1);
        this.value2 = value2;
    }

    public int getValue2() {
        return value2;
    }
}
