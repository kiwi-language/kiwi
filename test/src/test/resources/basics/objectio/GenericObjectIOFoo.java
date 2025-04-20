package objectio;

import java.io.*;

public class GenericObjectIOFoo<E> implements Serializable {

    private E value;

    public GenericObjectIOFoo(E value) {
        this.value = value;
    }

    public E getValue() {
        return value;
    }

    @Serial
    private void writeObject(ObjectOutputStream s) throws IOException {
        s.writeObject(value);
    }

    @Serial
    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        value = (E) s.readObject();
    }

}
