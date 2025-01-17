package org.metavm.autograph.mocks;

import org.metavm.annotation.NativeEntity;
import org.metavm.api.Entity;
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

@NativeEntity(79)
@Entity(compiled = true)
public class AstExceptionFoo extends org.metavm.entity.Entity {

    @SuppressWarnings("unused")
    private static Klass __klass__;
    private int amount;

    @Nullable
    public String errorMessage;

    public int executionCount;

    @Generated
    public static void visitBody(StreamVisitor visitor) {
        visitor.visitInt();
        visitor.visitNullable(visitor::visitUTF);
        visitor.visitInt();
    }

    public void test(int dec) {
        try {
            if (dec <= 0) {
                throw new RuntimeException("Illegal arguments");
            }
            if (dec > amount) {
                throw new AstException();
            }
            amount -= dec;
            errorMessage = null;
        } catch (AstException e) {
            var message = e.getMessage();
            if (message != null) {
                errorMessage = message;
            } else {
                errorMessage = "Execution failed";
            }
        } finally {
            executionCount++;
        }
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
        return EntityRegistry.TAG_AstExceptionFoo;
    }

    @Generated
    @Override
    public void readBody(MvInput input, org.metavm.entity.Entity parent) {
        this.amount = input.readInt();
        this.errorMessage = input.readNullable(input::readUTF);
        this.executionCount = input.readInt();
    }

    @Generated
    @Override
    public void writeBody(MvOutput output) {
        output.writeInt(amount);
        output.writeNullable(errorMessage, output::writeUTF);
        output.writeInt(executionCount);
    }

    @Override
    protected void buildSource(Map<String, org.metavm.object.instance.core.Value> source) {
    }
}
