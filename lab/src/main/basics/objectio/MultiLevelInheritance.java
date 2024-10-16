package objectio;

import java.io.*;

public class MultiLevelInheritance extends Base2 implements Serializable {

    @Serial
    private void writeObject(ObjectOutputStream s) throws IOException {
        s.defaultWriteObject();
    }

    @Serial
    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        s.defaultReadObject();
        modCount++;
    }

}

class Base1 {

    transient int modCount;

    public int getModCount() {
        return modCount;
    }
}

class Base2 extends Base1 {}
