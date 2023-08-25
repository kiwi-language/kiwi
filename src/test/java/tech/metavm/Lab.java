package tech.metavm;

import java.util.List;

public class Lab {

    public static void main(String[] args) {
        System.out.println(test(1));
    }

    private static int test(int v) {
        int u = 0;
        if(cond(v))  {
            u += v;
        }
        return u;
    }

    private static boolean cond(int v) {
        return v > 0;
    }

    public static <T extends String> void lab(List<? super String> t) {

    }

}