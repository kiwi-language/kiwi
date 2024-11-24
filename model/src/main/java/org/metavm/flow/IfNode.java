package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.api.EntityType;
import org.metavm.entity.ElementVisitor;
import org.metavm.expression.ExpressionTypeMap;

import javax.annotation.Nullable;

@EntityType
public class IfNode extends JumpNode {

    private transient ExpressionTypeMap nextExpressionTypes;

    public IfNode(@NotNull String name, @Nullable Node previous, @NotNull Code code,
                  Node target) {
        super(name, null, previous, code);
//        var narrower = new TypeNarrower(getExpressionTypes()::getType);
//        mergeExpressionTypes(narrower.narrowType(Expressions.not(condition.getExpression())));
        if(target != null)
            setTarget(target);
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitIfNode(this);
    }

    @Override
    public boolean hasOutput() {
        return false;
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("if " + getTarget().getName());
    }

    @Override
    public ExpressionTypeMap getNextExpressionTypes() {
//        if(nextExpressionTypes == null) {
//            var curExprTypes = getExpressionTypes();
//            var narrower = new TypeNarrower(curExprTypes::getType);
//            nextExpressionTypes = curExprTypes.merge(narrower.narrowType(Expressions.not(condition.getExpression())));
//        }
        return nextExpressionTypes;
    }

    @Override
    public int getStackChange() {
        return -1;
    }

    @Override
    public void writeCode(CodeOutput output) {
        output.write(Bytecodes.IF);
        output.writeShort(getTarget().getOffset());
    }

    @Override
    public int getLength() {
        return 3;
    }

}
