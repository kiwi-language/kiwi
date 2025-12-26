package org.metavm.flow;

import org.metavm.api.Entity;
import org.metavm.entity.ElementVisitor;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.type.ClassType;

import java.util.function.Consumer;

@Entity
public class NewChildNode extends NewObjectNode {

    public NewChildNode(String name, ClassType type, Node prev, Code code) {
        super(name, type, prev, code, false, false);
    }

    public static Node read(CodeInput input, String name) {
        return new NewChildNode(name, (ClassType) input.readConstant(), input.getPrev(), input.getCode());
    }

    @Override
    public int getStackChange() {
        return 0;
    }

    @Override
    public void writeCode(CodeOutput output) {
        output.write(Bytecodes.NEW_CHILD);
        output.writeConstant(getType());
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("newchild " + getType());
    }

    @Override
    public int getLength() {
        return 3;
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitNewChildNode(this);
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
