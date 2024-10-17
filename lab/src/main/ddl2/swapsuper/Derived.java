package swapsuper;

public class Derived extends Base2 {
    private final int value3;

    public Derived(int value1, int value2, int value3) {
        super(value1, value2);
        this.value3 = value3;
    }

    public int getValue3() {
        return value3;
    }
}
