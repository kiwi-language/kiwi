package org.metavm.expression;

import org.metavm.annotation.NativeEntity;
import org.metavm.api.Entity;
import org.metavm.api.Generated;
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

@NativeEntity(80)
@Entity
public class TypeReducerFoo extends org.metavm.entity.Entity {

    @SuppressWarnings("unused")
    private static Klass __klass__;
    @Nullable
    public String code;

    public int amount;

    @Generated
    public static void visitBody(StreamVisitor visitor) {
        visitor.visitNullable(visitor::visitUTF);
        visitor.visitInt();
    }

    @Nullable
    @Override
    public org.metavm.entity.Entity getParentEntity() {
        return null;
    }

    @Override
    public void forEachReference(Consumer<Reference> action) {
    }

    @Override
    public void buildJson(Map<String, Object> map) {
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
        return EntityRegistry.TAG_TypeReducerFoo;
    }

    @Generated
    @Override
    public void readBody(MvInput input, org.metavm.entity.Entity parent) {
        this.code = input.readNullable(input::readUTF);
        this.amount = input.readInt();
    }

    @Generated
    @Override
    public void writeBody(MvOutput output) {
        output.writeNullable(code, output::writeUTF);
        output.writeInt(amount);
    }

    @Override
    protected void buildSource(Map<String, org.metavm.object.instance.core.Value> source) {
    }
}
