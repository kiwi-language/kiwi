package tech.metavm.autograph.mocks;

import tech.metavm.util.InternalException;

import java.util.List;

public class TransformFoo {

    public int test(List<String> names, String searchFor) {
        int index  = -1;
        int i = 0;
        for (String name : names) {
            if(name.equals(searchFor)) {
                index = i;
                break;
            }
            i++;
        }
        if(index == -1) throw new InternalException("Not found");
        return index;
    }

}
