package org.manul.flow;

import org.jetbrains.annotations.NotNull;
import org.manul.api.Entity;
import org.manul.common.ErrorCode;
import org.manul.entity.ElementVisitor;
import org.manul.object.instance.core.Instance;
import org.manul.object.instance.core.Reference;
import org.manul.util.BusinessException;

import java.util.function.Consumer;

@Entity
public class ClearArrayNode extends Node {

    public ClearArrayNode(String name,
                          Node previous, Code code) {
        super(name, null, previous, code);
    }

    public static Node read(CodeInput input, String name) {
        return new ClearArrayNode(name, input.getPrev(), input.getCode());
    }

    private Value check(@NotNull Value array) {
        if(!array.getType().isArray())
            throw new BusinessException(ErrorCode.NOT_AN_ARRAY_VALUE);
        return array;
    }

    @Override
    public boolean hasOutput() {
        return false;
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("arrayclear");
    }

    @Override
    public int getStackChange() {
        return -1;
    }

    @Override
    public void writeCode(CodeOutput output) {
        output.write(Bytecodes.CLEAR_ARRAY);
    }

    @Override
    public int getLength() {
        return 1;
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitClearArrayNode(this);
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
    public void forEachChild(Consumer<? super Instance> action) {
        super.forEachChild(action);
    }
}
