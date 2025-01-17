package org.metavm.mocks;

import org.metavm.annotation.NativeEntity;
import org.metavm.api.Generated;
import org.metavm.entity.Entity;
import org.metavm.entity.EntityRegistry;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.type.ClassType;
import org.metavm.object.type.Klass;
import org.metavm.util.MvInput;
import org.metavm.util.MvOutput;
import org.metavm.util.StreamVisitor;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.function.Consumer;

@NativeEntity(85)
public class ValueFoo extends Entity {

    @SuppressWarnings("unused")
    private static Klass __klass__;
    private ClassType classType;

    public ValueFoo(ClassType classType) {
        this.classType = classType;
    }

    @Generated
    public static void visitBody(StreamVisitor visitor) {
        visitor.visitValue();
    }

    public ClassType getClassType() {
        return classType;
    }

    @Nullable
    @Override
    public Entity getParentEntity() {
        return null;
    }

    @Override
    public void forEachReference(Consumer<Reference> action) {
        classType.forEachReference(action);
    }

    @Override
    public void buildJson(Map<String, Object> map) {
        map.put("classType", this.getClassType().toJson());
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
    }

    @Override
    public int getEntityTag() {
        return EntityRegistry.TAG_ValueFoo;
    }

    @Generated
    @Override
    public void readBody(MvInput input, Entity parent) {
        this.classType = (ClassType) input.readType();
    }

    @Generated
    @Override
    public void writeBody(MvOutput output) {
        output.writeValue(classType);
    }

    @Override
    protected void buildSource(Map<String, org.metavm.object.instance.core.Value> source) {
    }
}
