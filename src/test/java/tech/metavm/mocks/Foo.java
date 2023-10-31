package tech.metavm.mocks;

import tech.metavm.entity.*;
import tech.metavm.util.ReadWriteArray;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

@EntityType("傻")
public class Foo extends Entity {

    public static final IndexDef<Foo> IDX_NAME = new IndexDef<>(Foo.class, false, "name");

    @EntityField(value = "名称", asTitle = true)
    private String name;

    @EntityField(value = "编号")
    @Nullable
    private String code;

    @ChildEntity("巴")
    private Bar bar;

    @EntityField("量子X")
    @Nullable
    private Qux qux;

    @EntityField("量子X2")
    @Nullable
    private Qux qux2;

    @EntityField("巴子")
    @Nullable
    private ReadWriteArray<Baz> bazList;

    public Foo(String name, Bar bar) {
        this.name = name;
        this.bar = bar;
    }

    public Foo(String name, Bar bar, @Nullable Qux qux, @Nullable List<Baz> bazList) {
        this.name = name;
        this.bar = bar;
        this.qux = qux;
        this.bazList = new ReadWriteArray<>(Baz.class, bazList);
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
        this.bazList = bazList != null ? new ReadWriteArray<>(Baz.class, bazList) : null;
    }

    @Nullable
    public String getCode() {
        return code;
    }

    public void setCode(@Nullable String code) {
        this.code = code;
    }

    @Override
    public String toString() {
        return "Foo{" +
                "name='" + name + '\'' +
                ", bar=" + bar +
                ", qux=" + qux +
                ", bazList=" + bazList +
                '}';
    }
}
