package swapsuper;

public class Base1 extends Base2 {
    private final int value1;

    public Base1(int value1, int value2) {
        super(value2);
        this.value1 = value1;
    }

    public int getValue1() {
        return value1;
    }
}
