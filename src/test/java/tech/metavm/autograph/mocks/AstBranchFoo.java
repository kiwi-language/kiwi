package tech.metavm.autograph.mocks;

import tech.metavm.entity.Entity;

public class AstBranchFoo extends Entity {

    private int count;

    public int test(Object value) {
        int result;
        if(value instanceof AstBranchFoo foo || value instanceof String || value instanceof Long) {
            result = 1;
        }
        else {
            result = 0;
        }
        return result;
    }

    public int getCount() {
        return count;
    }

}
