package tech.metavm.util;

import tech.metavm.entity.ChildEntity;
import tech.metavm.entity.Entity;
import tech.metavm.entity.EntityField;
import tech.metavm.entity.EntityType;

@EntityType("傻")
public class Foo extends Entity {

    @EntityField(value = "名称", asTitle = true)
    private String name;

    @ChildEntity("巴")
    private Bar bar;

    public Foo(String name, Bar bar) {
        this.name = name;
        this.bar = bar;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Bar getBar() {
        return bar;
    }

    public void setBar(Bar bar) {
        this.bar = bar;
    }
}
