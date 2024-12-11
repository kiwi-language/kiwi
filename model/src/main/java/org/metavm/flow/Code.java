package org.metavm.flow;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.metavm.api.ChildEntity;
import org.metavm.api.Entity;
import org.metavm.entity.*;
import org.metavm.object.instance.core.Id;
import org.metavm.util.EncodingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.LinkedList;
import java.util.List;

@Entity
public class Code extends Element implements LoadAware {

    public static final Logger debugLogger = LoggerFactory.getLogger("Debug");

    private final Callable callable;
    @ChildEntity
    private ChildArray<Node> nodes = addChild(new ChildArray<>(Node.class), "nodes");
    private int maxLocals;
    private int maxStack;
    private String codeBase64 = EncodingUtils.encodeBase64(new byte[0]);
    private transient byte[] code;

    public Code(Callable callable) {
        super(null, null);
        this.callable = callable;
        nodes.setEphemeralEntity(true);
    }

    public void addNode(Node node) {
        onNodeChange();
        var prev = node.getPredecessor() != null ? node.getPredecessor() : null;
        if (prev != null) {
            nodes.addChildAfter(node, prev);
        } else {
            var nodes = this.nodes;
            if (!nodes.isEmpty())
                nodes.get(0).insertBefore(node);
            nodes.addFirstChild(node);
        }
        if(node instanceof VariableAccessNode varAccNode)
            maxLocals = Math.max(maxLocals, varAccNode.getIndex() + 1);
        getFlow().addNode(node);
    }

    public void clear() {
        onNodeChange();
        this.nodes.clear();
        codeBase64 = EncodingUtils.encodeBase64(new byte[0]);
        code = null;
        maxLocals = 0;
        maxStack = 0;
    }

    public void setNodes(List<Node> nodes) {
        onNodeChange();
        this.nodes.resetChildren(nodes);
    }

    public Node getNode(long id) {
        return nodes.get(org.metavm.entity.Entity::tryGetId, id);
    }

    public Node getNode(Id id) {
        return nodes.get(org.metavm.entity.Entity::tryGetId, id);
    }

    public List<Node> getNodes() {
        return nodes.toList();
    }

    public Node getNodeById(long id) {
        return nodes.get(org.metavm.entity.Entity::tryGetId, id);
    }

    public Node getNodeByName(String name) {
        return nodes.get(Node::getName, name);
    }

    public Node getNodeByIndex(int index) {
        return nodes.get(index);
    }

    public Node tryGetFirstNode() {
        return nodes.isEmpty() ? null : nodes.get(0);
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
        return nodes.isEmpty() ? null : nodes.get(nodes.size() - 1);
    }

    private void onNodeChange() {
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitScope(this);
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
        var tryEnters = new LinkedList<TryEnterNode>();
        int offset = 0;
        for (Node node : nodes) {
            node.setOffset(offset);
            offset += node.getLength();
            if(node instanceof TryEnterNode tryEnter)
                tryEnters.push(tryEnter);
            else if(node instanceof TryExitNode tryExit)
                tryEnters.pop().setExit(tryExit);
        }
        assert tryEnters.isEmpty();
        var output = new CodeOutput(getFlow().getConstantPool());
        nodes.forEach(node -> node.writeCode(output));
        code = output.toByteArray();
        codeBase64 = EncodingUtils.encodeBase64(code);
    }

    @Override
    public void onLoadPrepare() {
        nodes = addChild(new ChildArray<>(Node.class), "nodes");
        nodes.setEphemeralEntity(true);
        code = EncodingUtils.decodeBase64(codeBase64);
    }

    @Override
    public void onLoad() {
    }

    public void setCodeBase64(String codebase64) {
        this.codeBase64 = codebase64;
        this.code = EncodingUtils.decodeBase64(codebase64);
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

    public void write(KlassOutput output) {
        output.writeInt(maxLocals);
        output.writeInt(maxStack);
        output.writeInt(code.length);
        output.write(code);
    }

    public void read(KlassInput input) {
        maxLocals = input.readInt();
        maxStack = input.readInt();
        code = new byte[input.readInt()];
        int n = input.read(code);
        codeBase64 = EncodingUtils.encodeBase64(code);
    }

}
