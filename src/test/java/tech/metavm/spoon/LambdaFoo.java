package tech.metavm.spoon;

import tech.metavm.entity.Identifiable;

import java.util.Comparator;
import java.util.Map;

public class LambdaFoo {

//    public Identifiable getIdentifiable() {
//        return () -> 1L;
//    }
//
//    public Comparator<Integer> getComparator2() {
//        return new Comparator<Integer>() {
//            @Override
//            public int compare(Integer o1, Integer o2) {
//                return Integer.compare(o2, o1);
//            }
//        };
//    }

    public void testConsumer(Map<String, Object> map) {
        map.forEach((k,v) -> System.out.println(k + ": " + v));
    }

}
