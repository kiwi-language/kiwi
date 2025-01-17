package org.metavm.mocks;

import org.metavm.annotation.NativeEntity;
import org.metavm.api.Entity;
import org.metavm.api.EntityField;
import org.metavm.api.Generated;
import org.metavm.api.NativeApi;
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

@NativeEntity(86)
@Entity
public class Bar extends org.metavm.entity.Entity {
    @SuppressWarnings("unused")
    private static Klass __klass__;
    private @Nullable Foo foo;
    @EntityField(asTitle = true)
    private String code;

    public Bar(@Nullable Foo foo, String code) {
        this.foo = foo;
        this.code = code;
    }

    @Generated
    public static void visitBody(StreamVisitor visitor) {
        visitor.visitUTF();
    }

    public String code() {
        return code;
    }

    @Nullable
    @Override
    public org.metavm.entity.Entity getParentEntity() {
        return foo;
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
        return EntityRegistry.TAG_Bar;
    }

    @Generated
    @Override
    public void readBody(MvInput input, org.metavm.entity.Entity parent) {
        this.foo = (Foo) parent;
        this.code = input.readUTF();
    }

    @Generated
    @Override
    public void writeBody(MvOutput output) {
        output.writeUTF(code);
    }

    @Override
    protected void buildSource(Map<String, org.metavm.object.instance.core.Value> source) {
    }
}
