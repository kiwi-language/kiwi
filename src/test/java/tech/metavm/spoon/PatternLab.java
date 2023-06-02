package tech.metavm.spoon;

public class PatternLab {

    public static void test(Object obj) {
        if(obj instanceof String str) {
            System.out.println(str.length());
        }
    }

}