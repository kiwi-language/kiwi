package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.StdKlass;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.type.ClassType;
import org.metavm.object.type.Klass;
import org.metavm.object.type.KlassType;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.function.Consumer;

public class LoadInnerKlassTypeNode extends Node {

    @SuppressWarnings("unused")
    private static Klass __klass__;
    private final KlassType type;

    public LoadInnerKlassTypeNode(@NotNull String name, @Nullable Node previous, @NotNull Code code, KlassType type) {
        super(name, StdKlass.klassType.type(), previous, code);
        this.type = type;
    }

    public static Node read(CodeInput input, String name) {
        return new LoadInnerKlassTypeNode(name, input.getPrev(), input.getCode(), (KlassType) input.readConstant());
    }

    @Override
    public boolean hasOutput() {
        return true;
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("ltinnerkt " + type.getTypeDesc());
    }

    @Override
    public int getStackChange() {
        return -type.getKlass().getTypeParameters().size();
    }

    @Override
    public void writeCode(CodeOutput output) {
        output.write(Bytecodes.LT_INNER_KLASS);
        output.writeConstant(type);
    }

    @Override
    public int getLength() {
        return 3;
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitLoadInnerKlassTypeNode(this);
    }

    @Override
    public void acceptChildren(ElementVisitor<?> visitor) {
        super.acceptChildren(visitor);
        type.accept(visitor);
    }

    @Override
    public void forEachReference(Consumer<Reference> action) {
        super.forEachReference(action);
        type.forEachReference(action);
    }

    @Override
    public void buildJson(Map<String, Object> map) {
        map.put("stackChange", this.getStackChange());
        map.put("length", this.getLength());
        map.put("flow", this.getFlow().getStringId());
        map.put("name", this.getName());
        var successor = this.getSuccessor();
        if (successor != null) map.put("successor", successor.getStringId());
        var predecessor = this.getPredecessor();
        if (predecessor != null) map.put("predecessor", predecessor.getStringId());
        map.put("code", this.getCode().toJson());
        map.put("exit", this.isExit());
        map.put("unconditionalJump", this.isUnconditionalJump());
        map.put("sequential", this.isSequential());
        var error = this.getError();
        if (error != null) map.put("error", error);
        var type = this.getType();
        if (type != null) map.put("type", type.toJson());
        map.put("expressionTypes", this.getExpressionTypes());
        map.put("nextExpressionTypes", this.getNextExpressionTypes());
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
