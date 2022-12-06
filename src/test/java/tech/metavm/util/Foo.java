package tech.metavm.util;

import tech.metavm.entity.*;
import javax.annotation.Nullable;
import java.util.List;

@EntityType("傻")
public class Foo extends Entity {

    public static final IndexDef<Foo> IDX_NAME = new IndexDef<>(Foo.class, "name");

    @EntityField(value = "名称", asTitle = true)
    private String name;

    @ChildEntity("巴")
    private Bar bar;

    @EntityField("量子X")
    @Nullable
    private Qux qux;

    @EntityField("巴子")
    @Nullable
    private Table<Baz> bazList;

    public Foo(String name, Bar bar) {
        setName(name);
        setBar(bar);
    }

    public Foo(String name, Bar bar, @Nullable Qux qux, @Nullable List<Baz> bazList) {
        setName(name);
        setBar(bar);
        setQux(qux);
        setBazList(bazList);
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

    @Nullable
    public Qux getQux() {
        return qux;
    }

    public void setQux(@Nullable Qux qux) {
        this.qux = qux;
    }

    @Nullable
    public Table<Baz> getBazList() {
        return bazList;
    }

    public void setBazList(@Nullable List<Baz> bazList) {
        this.bazList = new Table<>(bazList);
    }
}
