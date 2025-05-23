package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.api.Entity;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.StdKlass;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.type.ClassType;
import org.metavm.object.type.IndexRef;
import org.metavm.object.type.Klass;
import org.metavm.object.type.KlassType;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@Entity
public class IndexSelectNode extends Node {

    @SuppressWarnings("unused")
    private static Klass __klass__;
    private final IndexRef indexRef;

    public IndexSelectNode(String name, Node previous, Code code,
                           IndexRef indexRef) {
        super(name, null, previous, code);
        this.indexRef = indexRef;
    }

    public static Node read(CodeInput input, String name) {
        return new IndexSelectNode(name, input.getPrev(), input.getCode(), (IndexRef) input.readConstant());
    }

    @Override
    @NotNull
    public ClassType getType() {
        return new KlassType(null, StdKlass.arrayList.get(), List.of(indexRef.getDeclaringType()));
    }

    @Override
    public boolean hasOutput() {
        return true;
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("indexSelect(" + indexRef.getName() + ")");
    }

    @Override
    public int getStackChange() {
        return 0;
    }

    @Override
    public void writeCode(CodeOutput output) {
        output.write(Bytecodes.INDEX_SELECT);
        output.writeConstant(indexRef);
    }

    @Override
    public int getLength() {
        return 3;
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitIndexSelectNode(this);
    }

    @Override
    public void acceptChildren(ElementVisitor<?> visitor) {
        super.acceptChildren(visitor);
        indexRef.accept(visitor);
    }

    @Override
    public void forEachReference(Consumer<Reference> action) {
        super.forEachReference(action);
        indexRef.forEachReference(action);
    }

    @Override
    public void buildJson(Map<String, Object> map) {
        map.put("type", this.getType().toJson());
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
