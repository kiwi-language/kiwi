package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.api.Entity;
import org.metavm.entity.ElementVisitor;
import org.metavm.expression.ExpressionTypeMap;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.Reference;

import javax.annotation.Nullable;
import java.util.function.Consumer;

@Entity
public class IfEqNode extends BranchNode {

    private transient ExpressionTypeMap nextExpressionTypes;

    public IfEqNode(@NotNull String name, @Nullable Node previous, @NotNull Code code,
                    LabelNode target) {
        super(name, null, previous, code);
        if(target != null)
            setTarget(target);
    }

    public static Node read(CodeInput input, String name) {
        return new IfEqNode(name, input.getPrev(), input.getCode(), input.readLabel());
    }

    @Override
    public boolean hasOutput() {
        return false;
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("ifeq " + getTarget().getName());
    }

    @Override
    public ExpressionTypeMap getNextExpressionTypes() {
//        if(nextExpressionTypes == null) {
//            var curExprTypes = getExpressionTypes();
//            var narrower = new TypeNarrower(curExprTypes::getType);
//            nextExpressionTypes = curExprTypes.merge(narrower.narrowType(condition.getExpression()));
//        }
        return nextExpressionTypes;
    }

    @Override
    public int getStackChange() {
        return -1;
    }

    @Override
    public void writeCode(CodeOutput output) {
        output.write(Bytecodes.IF_EQ);
        output.writeShort(getTarget().getOffset() - getOffset());
    }

    @Override
    public int getLength() {
        return 3;
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitIfEqNode(this);
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
