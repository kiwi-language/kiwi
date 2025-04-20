package array;

import java.lang.reflect.Array;

public class ReflectNewArrayFoo {

    private final Object[] array;

    public ReflectNewArrayFoo(Object[] a) {
        array = (Object[]) Array.newInstance(a.getClass().getComponentType(), 10);
    }

    public Object get(int i) {
        return array[i];
    }

}
