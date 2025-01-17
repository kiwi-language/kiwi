package org.metavm.flow;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.jetbrains.annotations.NotNull;
import org.metavm.annotation.NativeEntity;
import org.metavm.api.ChildEntity;
import org.metavm.api.Entity;
import org.metavm.api.Generated;
import org.metavm.entity.*;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.EntityRegistry;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.type.ClassType;
import org.metavm.object.type.Klass;
import org.metavm.util.MvInput;
import org.metavm.util.MvOutput;
import org.metavm.util.StreamVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.*;
import java.util.Map;
import java.util.function.Consumer;

@NativeEntity(61)
@Entity
public class Code extends org.metavm.entity.Entity implements Element, LocalKey {

    public static final Logger debugLogger = LoggerFactory.getLogger("Debug");
    @SuppressWarnings("unused")
    private static Klass __klass__;

    private Callable callable;
    @ChildEntity
    private transient LinkedList<Node> nodes = new LinkedList<>();
    private int maxLocals;
    private int maxStack;
    private byte[] code;

    public Code(Callable callable) {
        super();
        this.callable = callable;
    }

    @Generated
    public static void visitBody(StreamVisitor visitor) {
        visitor.visitInt();
        visitor.visitInt();
        visitor.visitBytes();
    }

    public void addNode(Node node) {
        onNodeChange();
        var prev = node.getPredecessor() != null ? node.getPredecessor() : null;
        if (prev != null) {
            addNodeAfter(node, prev);
        } else {
            var nodes = this.nodes;
            if (!nodes.isEmpty())
                nodes.getFirst().insertBefore(node);
            nodes.addFirst(node);
        }
        if(node instanceof VariableAccessNode varAccNode)
            maxLocals = Math.max(maxLocals, varAccNode.getIndex() + 1);
        getFlow().addNode(node);
    }

    public void addNodeAfter(Node node, Node anchor) {
        var it = nodes.listIterator();
        while (it.hasNext()) {
            if (it.next().equals(anchor)) {
                it.add(node);
                return;
            }
        }
        throw new IllegalStateException("Anchor " + anchor + " not in the list");
    }

    public void addNodeBefore(Node node, Node anchor) {
        var it = nodes.listIterator();
        while (it.hasNext()) {
            if (it.next().equals(anchor)) {
                it.previous();
                it.add(node);
                return;
            }
        }
        throw new IllegalStateException("Anchor " + anchor + " not in the list");
    }

    public void clear() {
        onNodeChange();
        this.nodes.clear();
        code = null;
        maxLocals = 0;
        maxStack = 0;
    }

    public void setNodes(List<Node> nodes) {
        onNodeChange();
        this.nodes.clear();
        this.nodes.addAll(nodes);
    }

    public List<Node> getNodes() {
        return Collections.unmodifiableList(nodes);
    }

    public void removeNode(Node node) {
        onNodeChange();
        nodes.remove(node);
        getFlow().removeNode(node);
    }

    public Callable getCallable() {
        return callable;
    }

    @JsonIgnore
    public Flow getFlow() {
        return callable instanceof Flow flow ? flow : ((Lambda) callable).getFlow();
    }

    public boolean isEmpty() {
        return nodes.isEmpty();
    }

    public boolean isNotEmpty() {
        return !nodes.isEmpty();
    }

    @Nullable
    public Node getLastNode() {
        return nodes.isEmpty() ? null : nodes.getLast();
    }

    private void onNodeChange() {
    }

    public void writeCode(CodeWriter writer) {
        writer.write(" {");
        writer.indent();
        nodes.forEach(node -> node.write(writer));
        writer.unindent();
        writer.writeNewLine("}");
    }

    public String nextNodeName(String prefix) {
        return getFlow().nextNodeName(prefix);
    }

    public int getMaxLocals() {
        return maxLocals;
    }

    public int getFrameSize() {
        return maxLocals + maxStack;
    }

    public void setMaxLocals(int maxLocals) {
        this.maxLocals = maxLocals;
    }

    public int getMaxStack() {
        return maxStack;
    }

    public void setMaxStack(int maxStack) {
        this.maxStack = maxStack;
    }

    public int nextVariableIndex() {
        var i = Math.max(callable.getMinLocals(), maxLocals);
        maxLocals = i + 1;
        return i;
    }

    public void emitCode() {
        int offset = 0;
        for (Node node : nodes) {
            node.setOffset(offset);
            offset += node.getLength();
        }
        var output = new CodeOutput(getFlow().getConstantPool());
        nodes.forEach(node -> node.writeCode(output));
        code = output.toByteArray();
    }

    public void rebuildNodes() {
        assert nodes.isEmpty();
        new CodeInput(this).readNodes();
    }

    public byte[] getCode() {
        return code;
    }

    public int length() {
        return code.length;
    }

    public void setCode(byte[] code) {
        this.code = code;
    }

    @Override
    public String getTitle() {
        return "<code>";
    }

    @Nullable
    @Override
    public org.metavm.entity.Entity getParentEntity() {
        return (org.metavm.entity.Entity) callable;
    }

    @Override
    public boolean isValidLocalKey() {
        return true;
    }

    @Override
    public String getLocalKey(@NotNull BuildKeyContext context) {
        return "code";
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitCode(this);
    }

    @Override
    public void acceptChildren(ElementVisitor<?> visitor) {
        if (nodes != null) nodes.forEach(arg -> arg.accept(visitor));
    }

    private void onRead() {
        nodes = new LinkedList<>();
    }

    @Override
    public void forEachReference(Consumer<Reference> action) {
    }

    @Override
    public void buildJson(Map<String, Object> map) {
        map.put("nodes", this.getNodes());
        map.put("callable", this.getCallable());
        map.put("flow", this.getFlow().getStringId());
        map.put("empty", this.isEmpty());
        map.put("notEmpty", this.isNotEmpty());
        var lastNode = this.getLastNode();
        if (lastNode != null) map.put("lastNode", lastNode.getStringId());
        map.put("maxLocals", this.getMaxLocals());
        map.put("frameSize", this.getFrameSize());
        map.put("maxStack", this.getMaxStack());
        map.put("code", org.metavm.util.EncodingUtils.encodeBase64(this.getCode()));
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
        return EntityRegistry.TAG_Code;
    }

    @Generated
    @Override
    public void readBody(MvInput input, org.metavm.entity.Entity parent) {
        this.callable = (Callable) parent;
        this.maxLocals = input.readInt();
        this.maxStack = input.readInt();
        this.code = input.readBytes();
        this.onRead();
    }

    @Generated
    @Override
    public void writeBody(MvOutput output) {
        output.writeInt(maxLocals);
        output.writeInt(maxStack);
        output.writeBytes(code);
    }

    @Override
    protected void buildSource(Map<String, org.metavm.object.instance.core.Value> source) {
    }

    public void clearNodes() {
        nodes.clear();
    }
}
