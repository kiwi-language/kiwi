package array;

import java.util.Arrays;

public class ArrayUtilsFoo {

    public final String[] array = new String[]{"c", "b", "a"};
    public String[] copy;
    public Object[] copy2;

    public void sort() {
        //noinspection Convert2MethodRef,ComparatorCombinators
        Arrays.sort(array, 0, array.length, (s1, s2) -> s1.compareTo(s2));
    }

    public void sortAll() {
        //noinspection Convert2MethodRef,ComparatorCombinators
        Arrays.sort(array, (s1, s2) -> s1.compareTo(s2));
    }

    public String get(int i) {
        return array[i];
    }

    public void copy() {
        copy = Arrays.copyOf(array, array.length);
    }

    public void copy2() {
        copy2 = Arrays.copyOf(array, array.length, Object[].class);
    }

    public void systemCopy() {
        copy = new String[array.length];
        System.arraycopy(array, 0, copy, 1, 2);
    }

    public void copyRange(int from, int to) {
        copy = Arrays.copyOfRange(array, from, to);
    }

    public void copyRange2(int from, int to) {
        copy2 = Arrays.copyOfRange(array, from, to, Object[].class);
    }

    public String getCopy(int i) {
        return copy[i];
    }

    public Object getCopy2(int i) {
        return copy2[i];
    }
}
