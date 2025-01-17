package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.api.Entity;
import org.metavm.entity.ElementVisitor;
import org.metavm.expression.ExpressionTypeMap;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.type.ClassType;
import org.metavm.object.type.Klass;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.function.Consumer;

@Entity
public class IfNeNode extends BranchNode {

    @SuppressWarnings("unused")
    private static Klass __klass__;
    private transient ExpressionTypeMap nextExpressionTypes;

    public IfNeNode(@NotNull String name, @Nullable Node previous, @NotNull Code code,
                    LabelNode target) {
        super(name, null, previous, code);
//        var narrower = new TypeNarrower(getExpressionTypes()::getType);
//        mergeExpressionTypes(narrower.narrowType(Expressions.not(condition.getExpression())));
        if(target != null)
            setTarget(target);
    }

    public static Node read(CodeInput input, String name) {
        return new IfNeNode(name, input.getPrev(), input.getCode(), input.readLabel());
    }

    @Override
    public boolean hasOutput() {
        return false;
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("ifne " + getTarget().getName());
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
        output.write(Bytecodes.IF_NE);
        output.writeShort(getTarget().getOffset() - getOffset());
    }

    @Override
    public int getLength() {
        return 3;
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitIfNeNode(this);
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
    public void buildJson(Map<String, Object> map) {
        map.put("nextExpressionTypes", this.getNextExpressionTypes());
        map.put("stackChange", this.getStackChange());
        map.put("length", this.getLength());
        map.put("target", this.getTarget().getStringId());
        map.put("flow", this.getFlow().getStringId());
        map.put("name", this.getName());
        var successor = this.getSuccessor();
        if (successor != null) map.put("successor", successor.getStringId());
        var predecessor = this.getPredecessor();
        if (predecessor != null) map.put("predecessor", predecessor.getStringId());
        map.put("code", this.getCode().getStringId());
        map.put("exit", this.isExit());
        map.put("unconditionalJump", this.isUnconditionalJump());
        map.put("sequential", this.isSequential());
        var error = this.getError();
        if (error != null) map.put("error", error);
        var type = this.getType();
        if (type != null) map.put("type", type.toJson());
        map.put("expressionTypes", this.getExpressionTypes());
        map.put("text", this.getText());
        map.put("offset", this.getOffset());
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
        super.forEachChild(action);
    }
}
