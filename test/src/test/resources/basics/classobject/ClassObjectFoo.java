package classobject;

public class ClassObjectFoo<T> {

    public static boolean isInstance(Object object) {
        return object.getClass() == ClassObjectFoo.class;
    }

}
