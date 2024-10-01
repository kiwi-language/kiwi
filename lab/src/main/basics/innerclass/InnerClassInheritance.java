package innerclass;

public class InnerClassInheritance {

    private final String value;

    public InnerClassInheritance(String value) {
        this.value = value;
    }

    public String getValue() {
        return new Inner2(0).getValue();
    }

    public Inner2 inner2(int index) {
        return new Inner2(index);
    }

    private class Inner1 {

        public String getValue() {
            return value;
        }

    }

    private class Inner2 extends Inner1 {

        public Inner2(int index) {
        }
    }

}
