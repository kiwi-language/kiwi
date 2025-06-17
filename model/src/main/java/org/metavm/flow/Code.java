package org.metavm.flow;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.metavm.api.Generated;
import org.metavm.entity.*;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.type.Klass;
import org.metavm.util.MvInput;
import org.metavm.util.MvOutput;
import org.metavm.util.StreamVisitor;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@Slf4j
public class Code implements Element, LocalKey, Struct {

    private Callable callable;
    private transient LinkedList<Node> nodes = new LinkedList<>();
    private int maxLocals;
    private int maxStack;
    private byte[] code;

    public Code(Callable callable) {
        super();
        this.callable = callable;
    }

    @Generated
    public static Code read(MvInput input, Object parent) {
        var r = new Code((Callable) parent);
        r.maxLocals = input.readInt();
        r.maxStack = input.readInt();
        r.code = input.readBytes();
        r.onRead();
        return r;
    }

    @Generated
    public static void visit(StreamVisitor visitor) {
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
        writer.writeln(" {");
        writer.indent();
        for (Klass klass : getFlow().getKlasses()) {
            klass.writeCode(writer);
        }
        writer.writeln("max locals: " + maxLocals + ", max stack: " + maxStack);
        nodes.forEach(node -> node.write(writer));
        writer.unindent();
        writer.writeln("}");
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
        nodes.clear();
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

    public static Code read(MvInput input, org.metavm.entity.Entity parent) {
        var code = new Code((Callable) parent);
        code.callable = (Callable) parent;
        code.maxLocals = input.readInt();
        code.maxStack = input.readInt();
        code.code = input.readBytes();
        code.onRead();
        return code;
    }

    public void clearNodes() {
        nodes.clear();
    }

    public void forEachReference(Consumer<Reference> action) {
    }

    public void buildJson(Map<String, Object> map) {
        map.put("nodes", this.getNodes());
        map.put("callable", this.getCallable());
        map.put("flow", this.getFlow().getStringId());
        map.put("empty", this.isEmpty());
        map.put("notEmpty", this.isNotEmpty());
        var lastNode = this.getLastNode();
        if (lastNode != null) map.put("lastNode", lastNode.toJson());
        map.put("maxLocals", this.getMaxLocals());
        map.put("frameSize", this.getFrameSize());
        map.put("maxStack", this.getMaxStack());
        map.put("code", org.metavm.util.EncodingUtils.encodeBase64(this.getCode()));
    }

    @Generated
    public void write(MvOutput output) {
        output.writeInt(maxLocals);
        output.writeInt(maxStack);
        output.writeBytes(code);
    }

    public Map<String, Object> toJson() {
        var map = new java.util.HashMap<String, Object>();
        buildJson(map);
        return map;
    }
}
