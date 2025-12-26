package org.metavm.mocks;

import lombok.Getter;
import lombok.Setter;
import org.metavm.api.Entity;
import org.metavm.api.EntityField;
import org.metavm.wire.Wire;
import org.metavm.entity.IndexDef;
import org.metavm.entity.SearchField;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.instance.core.Value;
import org.metavm.util.Instances;
import org.metavm.util.Utils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@Wire(96)
@Entity(searchable = true)
public class Foo extends org.metavm.entity.Entity {

    public static final IndexDef<Foo> IDX_NAME = IndexDef.create(Foo.class,
            1, foo -> List.of(Instances.stringInstance(foo.name)));

    public static final IndexDef<Foo> IDX_ALL_FLAG = IndexDef.create(Foo.class,
            1, e -> List.of(Instances.booleanInstance(e.allFlag)));


    public static final SearchField<Foo> esName = SearchField.createTitle(0, "s0", foo -> Instances.stringInstance(foo.name));
    public static final SearchField<Foo> esCode = SearchField.createTitle(0, "s1",
            foo -> foo.code != null ? Instances.stringInstance(foo.code) : Instances.nullInstance());
    public static final SearchField<Foo> esQux = SearchField.createTitle(0, "r0", foo -> foo.qux);


    @Setter
    @Getter
    @EntityField(asTitle = true)
    private String name;

    @Nullable
    private String code;

    @Setter
    @Getter
    private Bar bar;

    @Nullable
    private Reference qux;

    @Nullable
    private Reference qux2;

    private List<Reference> bazList = new ArrayList<>();

    private final boolean allFlag = true;

    public Foo(Id id, String name, Bar bar) {
        super(id);
        this.name = name;
        this.bar = bar;
    }

    public Foo(Id id, String name, Bar bar, @Nullable Qux qux, List<Baz> bazList) {
        super(id);
        this.name = name;
        this.bar = bar;
        this.qux = Utils.safeCall(qux, Instance::getReference);
        bazList.forEach(baz -> this.bazList.add(baz.getReference()));
    }

    public void setData(Map<String, Object> data) {
        this.name = (String) data.get("name");
        this.bar = new Bar(nextChildId(), this, (String) data.get("barCode"));
    }

    @Nullable
    public Qux getQux() {
        return Utils.safeCall(qux, q -> (Qux) q.get());
    }

    public void setQux(@Nullable Qux qux) {
        this.qux = Utils.safeCall(qux, Instance::getReference);
    }

    @Nullable
    public Qux getQux2() {
        return Utils.safeCall(qux2, q -> (Qux) q.get());
    }

    public void setQux2(@Nullable Qux qux2) {
        this.qux2 = Utils.safeCall(qux2, Instance::getReference);
    }

    @Nullable
    public List<Baz> getBazList() {
        return bazList != null ? Utils.map(bazList, b -> (Baz) b.get()) : null;
    }

    public void setBazList(@Nullable List<Baz> bazList) {
        this.bazList = bazList != null ? new ArrayList<>(Utils.map(bazList, Instance::getReference)) : null;
    }

    @Nullable
    public String getCode() {
        return code;
    }

    public void setCode(@Nullable String code) {
        this.code = code;
    }

    @Nullable
    @Override
    public org.metavm.entity.Entity getParentEntity() {
        return null;
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

    @Override
    public void forEachReference(Consumer<Reference> action) {
        action.accept(bar.getReference());
        if (qux != null) action.accept(qux);
        if (qux2 != null) action.accept(qux2);
        for (var bazList_ : bazList) action.accept(bazList_);
    }

    @Override
    public void forEachChild(Consumer<? super Instance> action) {
        action.accept(bar);
    }

    @Override
    protected void buildSource(Map<String, Value> source) {
        source.put("l0." + esName.getColumn(), esName.getValue(this));
        source.put("l0." + esCode.getColumn(), esCode.getValue(this));
        source.put("l0." + esQux.getColumn(), esQux.getValue(this));
    }
}
