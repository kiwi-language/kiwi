package innerclass;

public abstract class InnerExtendsEnclosing<E> {

    public abstract boolean foo();

    private static class Inner<E> extends InnerExtendsEnclosing<E> {

        @Override
        public boolean foo() {
            return true;
        }
    }

}
