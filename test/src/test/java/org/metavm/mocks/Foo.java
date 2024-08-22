package org.metavm.mocks;

import org.metavm.api.ChildEntity;
import org.metavm.api.EntityField;
import org.metavm.api.EntityType;
import org.metavm.entity.Entity;
import org.metavm.entity.IndexDef;
import org.metavm.entity.NoProxy;
import org.metavm.entity.ReadWriteArray;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

@EntityType(searchable = true)
public class Foo extends Entity {

    public static final IndexDef<Foo> IDX_NAME = new IndexDef<>(Foo.class, false, "name");

    public static final IndexDef<Foo> IDX_ALL_FLAG = new IndexDef<>(Foo.class, false, "allFlag");

    @EntityField(asTitle = true)
    private String name;

    @Nullable
    private String code;

    @ChildEntity
    private Bar bar;

    @Nullable
    private Qux qux;

    @Nullable
    private Qux qux2;

    @ChildEntity
    @Nullable
    private ReadWriteArray<Baz> bazList;

    private boolean allFlag = true;

    public Foo(String name, Bar bar) {
        this.name = name;
        this.bar = addChild(bar, "bar");
    }

    public Foo(String name, Bar bar, @Nullable Qux qux, @Nullable List<Baz> bazList) {
        this.name = name;
        this.bar = addChild(bar, "bar");
        this.qux = qux;
        this.bazList = addChild(new ReadWriteArray<>(Baz.class, bazList), "bazList");
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

    @NoProxy
    public void setData(Map<String, Object> data) {
        this.name = (String) data.get("name");
        this.bar = new Bar((String) data.get("barCode"));
    }

    @Nullable
    public Qux getQux() {
        return qux;
    }

    public void setQux(@Nullable Qux qux) {
        this.qux = qux;
    }

    @Nullable
    public Qux getQux2() {
        return qux2;
    }

    public void setQux2(@Nullable Qux qux2) {
        this.qux2 = qux2;
    }

    @Nullable
    public ReadWriteArray<Baz> getBazList() {
        return bazList;
    }

    public void setBazList(@Nullable List<Baz> bazList) {
        this.bazList = bazList != null ?
                addChild(new ReadWriteArray<>(Baz.class, bazList), "bazList")
                : null;
    }

    @Nullable
    public String getCode() {
        return code;
    }

    public void setCode(@Nullable String code) {
        this.code = code;
    }

    @Override
    protected String toString0() {
        return "Foo{" +
                "name='" + name + '\'' +
                ", bar=" + bar +
                ", qux=" + qux +
                ", bazList=" + bazList +
                '}';
    }
}
