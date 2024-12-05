package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.api.EntityType;
import org.metavm.entity.ElementVisitor;
import org.metavm.expression.ExpressionTypeMap;

import javax.annotation.Nullable;

@EntityType
public class IfEqNode extends JumpNode {

    private transient ExpressionTypeMap nextExpressionTypes;

    public IfEqNode(@NotNull String name, @Nullable Node previous, @NotNull Code code,
                    Node target) {
        super(name, null, previous, code);
        if(target != null)
            setTarget(target);
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitIfEqNode(this);
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
        output.writeShort(getTarget().getOffset());
    }

    @Override
    public int getLength() {
        return 3;
    }

}
