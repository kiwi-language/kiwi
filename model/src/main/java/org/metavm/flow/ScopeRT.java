package org.metavm.flow;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.metavm.api.ChildEntity;
import org.metavm.api.EntityType;
import org.metavm.entity.*;
import org.metavm.flow.rest.ScopeDTO;
import org.metavm.object.instance.core.Id;
import org.metavm.util.DebugEnv;
import org.metavm.util.NncUtils;
import org.metavm.util.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.List;

@EntityType
public class ScopeRT extends Element {

    public static final Logger debugLogger = LoggerFactory.getLogger("Debug");

    private final Flow flow;
    @ChildEntity
    private final ChildArray<NodeRT> nodes = addChild(new ChildArray<>(new TypeReference<>() {
    }), "nodes");

    public ScopeRT(Flow flow) {
        this(flow, false);
    }

    public ScopeRT(Flow flow, boolean ephemeral) {
        super(null, null, ephemeral);
        this.flow = flow;
    }

    public ScopeDTO toDTO(boolean withNodes, SerializeContext serializeContext) {
        return new ScopeDTO(
                serializeContext.getStringId(this),
                withNodes ? NncUtils.map(getNodes(), nodeRT -> nodeRT.toDTO(serializeContext)) : List.of()
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
        flow.addNode(node);
    }

    public void clearNodes() {
        onNodeChange();
        this.nodes.clear();
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

}
