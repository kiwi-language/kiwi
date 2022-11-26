package tech.metavm.util;

public record Pair<T>(T first, T second) {

    public T any() {
        return NncUtils.anyNonNull(first, second);
    }

    @SuppressWarnings("unchecked")
    public <U> Pair<? extends U> cast(Class<U> uClass) {
        if(first == null || uClass.isInstance(first)
                && second == null || uClass.isInstance(second)) {
            return (Pair<? extends U>) this;
        }
        else {
            throw new ClassCastException();
        }
    }

}
