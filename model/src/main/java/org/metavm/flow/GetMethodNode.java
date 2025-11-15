package org.metavm.flow;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.metavm.api.Entity;
import org.metavm.entity.ElementVisitor;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.type.Type;

import javax.annotation.Nullable;
import java.util.function.Consumer;

@Getter
@Entity
public class GetMethodNode extends Node {

    private final MethodRef methodRef;

    public GetMethodNode(String name,
                         @Nullable Node previous,
                         @NotNull Code code,
                         MethodRef methodRef) {
        super(name, null, previous, code);
        this.methodRef = methodRef;
    }

    public static Node read(CodeInput input, String name) {
        return new GetMethodNode(name, input.getPrev(), input.getCode(), (MethodRef) input.readConstant());
    }

    @NotNull
    public Type getType() {
        return methodRef.getPropertyType();
    }

    @Override
    public boolean hasOutput() {
        return true;
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("getmethod " + methodRef);
    }

    @Override
    public int getStackChange() {
        return 0;
    }

    @Override
    public void writeCode(CodeOutput output) {
        output.write(Bytecodes.GET_METHOD);
        output.writeConstant(methodRef);
    }

    @Override
    public int getLength() {
        return 3;
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitGetMethodNode(this);
    }

    @Override
    public void acceptChildren(ElementVisitor<?> visitor) {
        super.acceptChildren(visitor);
        methodRef.accept(visitor);
    }

    @Override
    public void forEachReference(Consumer<Reference> action) {
        super.forEachReference(action);
        methodRef.forEachReference(action);
    }

    @Override
    public void forEachChild(Consumer<? super Instance> action) {
        super.forEachChild(action);
    }

}
