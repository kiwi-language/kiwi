package org.metavm.object.type;

import org.metavm.annotation.NativeEntity;
import org.metavm.api.Entity;
import org.metavm.api.Generated;
import org.metavm.entity.DummyGenericDeclaration;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.EntityRegistry;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.type.ClassType;
import org.metavm.object.type.Klass;
import org.metavm.util.MvInput;
import org.metavm.util.MvOutput;
import org.metavm.util.StreamVisitor;

import java.util.Map;
import java.util.function.Consumer;

@NativeEntity(25)
@Entity(ephemeral = true)
public class DummyTypeVariable extends TypeVariable {

    public static final DummyTypeVariable instance = new DummyTypeVariable();
    @SuppressWarnings("unused")
    private static Klass __klass__;

    private DummyTypeVariable() {
        super(null, "Dummy", DummyGenericDeclaration.INSTANCE);
    }

    @Generated
    public static void visitBody(StreamVisitor visitor) {
        TypeVariable.visitBody(visitor);
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void acceptChildren(ElementVisitor<?> visitor) {
        super.acceptChildren(visitor);
    }

    @Override
    public void forEachReference(Consumer<Reference> action) {
        super.forEachReference(action);
    }

    @Override
    public void buildJson(Map<String, Object> map) {
        map.put("genericDeclaration", this.getGenericDeclaration().getStringId());
        map.put("name", this.getName());
        map.put("qualifiedName", this.getQualifiedName());
        map.put("upperBound", this.getUpperBound().toJson());
        map.put("bounds", this.getBounds().stream().map(Type::toJson).toList());
        map.put("stage", this.getStage().name());
        map.put("typeDesc", this.getTypeDesc());
        map.put("type", this.getType().toJson());
        map.put("index", this.getIndex());
        map.put("attributes", this.getAttributes().stream().map(org.metavm.entity.Attribute::toJson).toList());
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
        super.forEachChild(action);
    }

    @Override
    public int getEntityTag() {
        return EntityRegistry.TAG_DummyTypeVariable;
    }

    @Generated
    @Override
    public void readBody(MvInput input, org.metavm.entity.Entity parent) {
        super.readBody(input, parent);
    }

    @Generated
    @Override
    public void writeBody(MvOutput output) {
        super.writeBody(output);
    }

    @Override
    protected void buildSource(Map<String, org.metavm.object.instance.core.Value> source) {
        super.buildSource(source);
    }
}
