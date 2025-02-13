package org.metavm.mocks;

import org.metavm.annotation.NativeEntity;
import org.metavm.api.EntityField;
import org.metavm.api.Entity;
import org.metavm.api.Generated;
import org.metavm.entity.EntityRegistry;
import org.metavm.entity.IndexDef;
import org.metavm.entity.NoProxy;
import org.metavm.entity.SearchField;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.instance.core.Value;
import org.metavm.object.type.ClassType;
import org.metavm.object.type.Klass;
import org.metavm.util.*;
import org.metavm.util.MvInput;
import org.metavm.util.MvOutput;
import org.metavm.util.StreamVisitor;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@NativeEntity(96)
@Entity(searchable = true)
public class Foo extends org.metavm.entity.Entity {

    public static final IndexDef<Foo> IDX_NAME = IndexDef.create(Foo.class,
            1, foo -> List.of(Instances.stringInstance(foo.name)));

    public static final IndexDef<Foo> IDX_ALL_FLAG = IndexDef.create(Foo.class,
            1, e -> List.of(Instances.booleanInstance(e.allFlag)));


    public static final SearchField<Foo> esName = SearchField.createTitle("s0", foo -> Instances.stringInstance(foo.name));
    public static final SearchField<Foo> esCode = SearchField.createTitle("s1",
            foo -> foo.code != null ? Instances.stringInstance(foo.code) : Instances.nullInstance());
    public static final SearchField<Foo> esQux = SearchField.createTitle("r0", foo -> foo.qux);
    @SuppressWarnings("unused")
    private static Klass __klass__;


    @EntityField(asTitle = true)
    private String name;

    @Nullable
    private String code;

    private Bar bar;

    @Nullable
    private Reference qux;

    @Nullable
    private Reference qux2;

    private List<Reference> bazList = new ArrayList<>();

    private boolean allFlag = true;

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

    @Generated
    public static void visitBody(StreamVisitor visitor) {
        visitor.visitUTF();
        visitor.visitNullable(visitor::visitUTF);
        visitor.visitEntity();
        visitor.visitNullable(visitor::visitValue);
        visitor.visitNullable(visitor::visitValue);
        visitor.visitList(visitor::visitValue);
        visitor.visitBoolean();
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
    public void buildJson(Map<String, Object> map) {
        map.put("name", this.getName());
        map.put("bar", this.getBar().getStringId());
        var qux = this.getQux();
        if (qux != null) map.put("qux", qux.getStringId());
        var qux2 = this.getQux2();
        if (qux2 != null) map.put("qux2", qux2.getStringId());
        var bazList = this.getBazList();
        if (bazList != null) map.put("bazList", bazList.stream().map(org.metavm.entity.Entity::getStringId).toList());
        var code = this.getCode();
        if (code != null) map.put("code", code);
    }

    @Override
    public Klass getInstanceKlass() {
        return __klass__;
    }

    @Override
    public ClassType getInstanceType() {
        return __klass__.getType();
    }

    @Override
    public void forEachChild(Consumer<? super Instance> action) {
        action.accept(bar);
    }

    @Override
    public int getEntityTag() {
        return EntityRegistry.TAG_Foo;
    }

    @Generated
    @Override
    public void readBody(MvInput input, org.metavm.entity.Entity parent) {
        this.name = input.readUTF();
        this.code = input.readNullable(input::readUTF);
        this.bar = input.readEntity(Bar.class, this);
        this.qux = input.readNullable(() -> (Reference) input.readValue());
        this.qux2 = input.readNullable(() -> (Reference) input.readValue());
        this.bazList = input.readList(() -> (Reference) input.readValue());
        this.allFlag = input.readBoolean();
    }

    @Generated
    @Override
    public void writeBody(MvOutput output) {
        output.writeUTF(name);
        output.writeNullable(code, output::writeUTF);
        output.writeEntity(bar);
        output.writeNullable(qux, output::writeValue);
        output.writeNullable(qux2, output::writeValue);
        output.writeList(bazList, output::writeValue);
        output.writeBoolean(allFlag);
    }

    @Override
    protected void buildSource(Map<String, Value> source) {
        source.put("l0." + esName.getColumn(), esName.getValue(this));
        source.put("l0." + esCode.getColumn(), esCode.getValue(this));
        source.put("l0." + esQux.getColumn(), esQux.getValue(this));
    }
}
