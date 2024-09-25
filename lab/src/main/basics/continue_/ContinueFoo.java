package continue_;

public class ContinueFoo {

    public static int oddIndexOf(int[] values, int v) {
        for (int i = 0; i < values.length; i++) {
            if(i % 2 == 0)
                continue;
            if(values[i] == v)
                return i;
            if(i > 100)
                break;
        }
        return -1;
    }

}
