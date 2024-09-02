package org.metavm.mocks;

import org.metavm.api.ChildEntity;
import org.metavm.api.EntityField;
import org.metavm.api.EntityType;
import org.metavm.entity.Entity;
import org.metavm.entity.ReadWriteArray;

import javax.annotation.Nullable;

@EntityType
public class UpgradeFoo extends Entity {

    private final String name;
    @EntityField(since = 1)
    private final Object bar;
    @EntityField
    private @Nullable Object value;
    @ChildEntity(since = 1)
    private final ReadWriteArray<UpgradeFoo> array = addChild(new ReadWriteArray<>(UpgradeFoo.class), "array");

    public UpgradeFoo(String name, Object bar) {
        this.name = name;
        this.bar = bar;
    }

    public String getName() {
        return name;
    }

    public Object getBar() {
        return bar;
    }

    @Nullable
    public Object getValue() {
        return value;
    }

    public void setValue(@Nullable Object value) {
        this.value = value;
    }

    public ReadWriteArray<UpgradeFoo> getArray() {
        return array;
    }

    @Override
    protected String toString0() {
        return "name: " + name + ", bar: " + bar;
    }
}