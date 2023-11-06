package tech.metavm;

public abstract class Lab {

    public static void main(String[] args) {

    }

}

class Base<T> {

    T value;

    T getValue() {
        return value;
    }

}

class Foo<E> extends Base<String> {}