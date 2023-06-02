package tech.metavm.spoon;

import java.util.List;

public class IteratorLab {

    public void test(List<?> list) {
        var iterator = list.iterator();
        if(iterator.hasNext()) {
            iterator.next();
        }
        while ((iterator.hasNext())) {
            System.out.println(iterator.next());
        }
    }

}
