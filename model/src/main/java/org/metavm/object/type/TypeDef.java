package org.metavm.object.type;

import org.jetbrains.annotations.NotNull;
import org.metavm.annotation.NativeEntity;
import org.metavm.api.Generated;
import org.metavm.entity.AttributedElement;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.EntityRegistry;
import org.metavm.entity.IndexDef;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.type.ClassType;
import org.metavm.object.type.Klass;
import org.metavm.util.Instances;
import org.metavm.util.MvInput;
import org.metavm.util.MvOutput;
import org.metavm.util.StreamVisitor;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@NativeEntity(21)
public abstract class TypeDef extends AttributedElement implements ITypeDef {

    @SuppressWarnings("unused")
    private static Klass __klass__;

    @Generated
    public static void visitBody(StreamVisitor visitor) {
        AttributedElement.visitBody(visitor);
    }

    public abstract @NotNull Type getType();

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
        map.put("type", this.getType().toJson());
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
        return EntityRegistry.TAG_TypeDef;
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
