package org.metavm.flow;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.metavm.api.ChildEntity;
import org.metavm.api.EntityType;
import org.metavm.entity.*;
import org.metavm.flow.rest.ScopeDTO;
import org.metavm.object.instance.core.Id;
import org.metavm.util.DebugEnv;
import org.metavm.util.EncodingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.LinkedList;
import java.util.List;

@EntityType
public class ScopeRT extends Element implements LoadAware {

    public static final Logger debugLogger = LoggerFactory.getLogger("Debug");

    private final Callable callable;
    private final Flow flow;
    @ChildEntity
    private ChildArray<NodeRT> nodes = addChild(new ChildArray<>(NodeRT.class), "nodes");
    private int maxLocals;
    private int maxStack;
    private String codeBase64 = EncodingUtils.encodeBase64(new byte[0]);
    private transient byte[] code;

    public ScopeRT(Callable callable, Flow flow) {
        super(null, null);
        this.callable = callable;
        this.flow = flow;
        nodes.setEphemeralEntity(true);
    }

    public ScopeDTO toDTO(SerializeContext serializeContext) {
        return new ScopeDTO(
                serializeContext.getStringId(this),
                codeBase64,
                maxLocals,
                maxStack
        );
    }

    private boolean shouldDebug() {
        if(DebugEnv.debugging && flow.getEffectiveHorizontalTemplate().getName().equals("find")) {
            return flow.getHorizontalTemplate() == null || flow.getTypeArguments().get(0).isCaptured();
        }
        else
            return false;
    }

    public void addNode(NodeRT node) {
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
        flow.addNode(node);
    }

    public void clear() {
        onNodeChange();
        this.nodes.clear();
        codeBase64 = EncodingUtils.encodeBase64(new byte[0]);
        code = null;
        maxLocals = 0;
        maxStack = 0;
    }

    public void setNodes(List<NodeRT> nodes) {
        onNodeChange();
        this.nodes.resetChildren(nodes);
    }

    public NodeRT getNode(long id) {
        return nodes.get(Entity::tryGetId, id);
    }

    public NodeRT getNode(Id id) {
        return nodes.get(Entity::tryGetId, id);
    }

    public List<NodeRT> getNodes() {
        return nodes.toList();
    }

    public NodeRT getNodeById(long id) {
        return nodes.get(Entity::tryGetId, id);
    }

    public NodeRT getNodeByName(String name) {
        return nodes.get(NodeRT::getName, name);
    }

    public NodeRT getNodeByIndex(int index) {
        return nodes.get(index);
    }

    public NodeRT tryGetFirstNode() {
        return nodes.isEmpty() ? null : nodes.get(0);
    }

    public void removeNode(NodeRT node) {
        onNodeChange();
        nodes.remove(node);
        flow.removeNode(node);
    }

    public Callable getCallable() {
        return callable;
    }

    @JsonIgnore
    public Flow getFlow() {
        return flow;
    }

    public boolean isEmpty() {
        return nodes.isEmpty();
    }

    public boolean isNotEmpty() {
        return !nodes.isEmpty();
    }

    @Nullable
    public NodeRT getLastNode() {
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
        return flow.nextNodeName(prefix);
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
        for (NodeRT node : nodes) {
            node.setOffset(offset);
            offset += node.getLength();
            if(node instanceof TryEnterNode tryEnter)
                tryEnters.push(tryEnter);
            else if(node instanceof TryExitNode tryExit)
                tryEnters.pop().setExit(tryExit);
        }
        assert tryEnters.isEmpty();
        var output = new CodeOutput(flow.getConstantPool());
        nodes.forEach(node -> node.writeCode(output));
        code = output.toByteArray();
        codeBase64 = EncodingUtils.encodeBase64(code);
    }

    @Override
    public void onLoadPrepare() {
        nodes = addChild(new ChildArray<>(NodeRT.class), "nodes");
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

    public void setCode(byte[] code) {
        this.code = code;
    }

}
