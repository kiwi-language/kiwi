package org.metavm.mocks;

import org.metavm.api.Entity;
import org.metavm.entity.IndexDef;

@Entity
public class IndexFoo extends org.metavm.entity.Entity {

    public static final IndexDef<IndexFoo> IDX_STATE = IndexDef.create(IndexFoo.class, "state");
    public static final IndexDef<IndexFoo> IDX_CODE = IndexDef.create(IndexFoo.class, "code");

    private FooState state = FooState.STATE1;
    private int code;

    public FooState getState() {
        return state;
    }

    public void setState(FooState state) {
        this.state = state;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }
}
