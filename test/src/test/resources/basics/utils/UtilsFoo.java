package utils;

import java.util.Objects;

public class UtilsFoo {

    public static int max(int v1, int v2) {
        return Math.max(v1, v2);
    }

    public static int checkIndex(int index, int length) {
        return Objects.checkIndex(index, length);
    }

}
