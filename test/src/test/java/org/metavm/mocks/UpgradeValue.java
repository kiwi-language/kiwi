package org.metavm.mocks;

import org.metavm.api.Entity;
import org.metavm.entity.IndexDef;

import javax.annotation.Nullable;

@Entity(since = 1)
public class UpgradeValue extends org.metavm.entity.Entity {

    public static final IndexDef<UpgradeValue> IDX_FOO = IndexDef.createUnique(UpgradeValue.class, "foo");

    private final UpgradeFoo foo;
    private @Nullable Object value;

    public UpgradeValue(UpgradeFoo foo) {
        this.foo = foo;
    }

    public UpgradeFoo getFoo() {
        return foo;
    }

    @Nullable
    public Object getValue() {
        return value;
    }

    public void setValue(@Nullable Object value) {
        this.value = value;
    }
}
