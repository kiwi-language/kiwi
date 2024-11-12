package org.metavm;

import java.util.List;

public class Lab {

    private int value;

    public int test(List<String> list, String s) {
        if(list.add(s))
            return 1;
        else
            return 0;
    }

}
