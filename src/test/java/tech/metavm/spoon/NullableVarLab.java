package tech.metavm.spoon;

import javax.annotation.Nullable;

public class NullableVarLab {

    public void test(@Nullable Integer i) {
        Integer j = i;
        System.out.println(j);
    }


}
