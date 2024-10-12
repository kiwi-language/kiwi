package objectio;

import org.metavm.api.ChildEntity;

import java.io.*;
import java.util.Arrays;

public class CustomObjectIOFoo extends List {

    private String id;

    public CustomObjectIOFoo(String id, Object[] elements) {
        super(elements);
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @Serial
    private void writeObject(ObjectOutputStream s) throws IOException {
        s.writeUTF(id);
    }

    @Serial
    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        id = s.readUTF();
    }

}

class List extends AbstractFoo {

    private int size;

    @ChildEntity
    private transient Object[] elements;

    public List(Object[] elements) {
        this.elements = Arrays.copyOf(elements, elements.length);
        size = elements.length;
    }

    public Object[] getElements() {
        return elements;
    }

    public void add(Object o) {
        if(size == elements.length)
            this.elements = Arrays.copyOf(elements, this.elements.length << 1);
        this.elements[size++] = o;
        modCount++;
    }

    public Object get(int i) {
        return elements[i];
    }

    @Serial
    private void writeObject(ObjectOutputStream s) throws IOException {
        s.defaultWriteObject();
        for (int i = 0; i < size; i++) {
            s.writeObject(elements[i]);
        }
    }

    @Serial
    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        s.defaultReadObject();
        elements = new Object[size];
        for (int i = 0; i < size; i++) {
            var e = s.readObject();
            elements[i] = e;
        }
    }

}


class AbstractFoo implements Serializable {

    protected transient int modCount;

    public int getModCount() {
        return modCount;
    }

}