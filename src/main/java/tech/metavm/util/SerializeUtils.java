package tech.metavm.util;

import java.io.*;

public class SerializeUtils {

    public static byte[] serialize(Object object) {
        try {
            var bout = new ByteArrayOutputStream();
            var out = new ObjectOutputStream(bout);
            out.writeObject(object);
            return bout.toByteArray();
        }
        catch (IOException e) {
            throw new InternalException(String.format("Fail to write object %s", object));
        }
    }

    public static <T> T deserialize(byte[] bytes, Class<T> klass) {
        try {
            ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes));
            return klass.cast(in.readObject());
        } catch (IOException | ClassNotFoundException e) {
            throw new InternalException(String.format("fail to read object of class: %s", klass.getName()), e);
        }
    }

}
