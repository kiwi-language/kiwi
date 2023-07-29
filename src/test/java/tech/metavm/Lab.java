package tech.metavm;

import java.util.Map;

public class Lab {

    public static void main(String[] args) {

    }

    public Object test(Object o) {
        return switch (o) {
            case String s -> s;
            case Long i -> i.toString();
            default -> o;
        };
    }

    private static void test(Map<String, Integer> map) {
        map.forEach((k,v) -> System.out.println(k + ": " + v));
    }


}