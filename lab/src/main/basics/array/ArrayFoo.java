package array;

public class ArrayFoo {

    private final Object[] array = new Object[5];
    private final int[] ints = new int[5];
    private final Object[][] multi = new Object[5][5];
    private final Object[][] initialized = new Object[][] {
            new Object[]{"metavm", "is", "the future"},
            new Object[] {1, 2, 3},
            {4, 5, 6}
    };

    public Object get(int i) {
        return array[i];
    }

    public void set(int i, Object value) {
        array[i] = value;
    }

    public int getInt(int i) {
        return ints[i];
    }

    public void setInt(int i, int v) {
        ints[i] = v;
    }

    public Object getMulti(int i, int j) {
        return multi[i][j];
    }

    public void setMulti(int i, int j, Object v) {
        multi[i][j] = v;
    }

    public Object getInitialized(int i, int j) {
        return initialized[i][j];
    }

}
