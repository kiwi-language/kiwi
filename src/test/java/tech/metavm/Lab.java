package tech.metavm;

public class Lab {

    public static void main(String[] args) {
        test(new Integer[] {1,2,3});
    }

    static void test(Object[] obj) {
        obj[1] = "s";
    }

}
