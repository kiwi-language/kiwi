package tech.metavm.flow;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.metavm.entity.*;
import tech.metavm.expression.ExpressionTypeMap;
import tech.metavm.expression.TypeNarrower;
import tech.metavm.flow.rest.ScopeDTO;
import tech.metavm.object.instance.core.Id;
import tech.metavm.util.DebugEnv;
import tech.metavm.util.NncUtils;
import tech.metavm.util.TypeReference;

import javax.annotation.Nullable;
import java.util.List;

@EntityType
public class ScopeRT extends Element {

    public static final Logger debugLogger = LoggerFactory.getLogger("Debug");

    private final Flow flow;
    @Nullable
    private final NodeRT owner;
    @ChildEntity
    private final ChildArray<NodeRT> nodes = addChild(new ChildArray<>(new TypeReference<>() {
    }), "nodes");
    private final boolean withBackEdge;
    @Nullable
    private Branch branch;

    private transient ExpressionTypeMap expressionTypes = ExpressionTypeMap.EMPTY;

    public ScopeRT(Flow flow) {
        this(flow, null, false, false);
    }

    public ScopeRT(Flow flow, @Nullable NodeRT owner) {
        this(flow, owner, false, false);
    }

    public ScopeRT(Flow flow, @Nullable NodeRT owner, boolean withBackEdge) {
        this(flow, owner, withBackEdge, false);
    }

    public ScopeRT(Flow flow, @Nullable NodeRT owner, boolean withBackEdge, boolean ephemeral) {
        super(null, null, ephemeral);
        this.flow = flow;
        this.owner = owner;
        this.withBackEdge = withBackEdge;
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

    public void setBranch(@Nullable Branch branch) {
        this.branch = branch;
        if (branch != null) {
            var typeNarrower = new TypeNarrower(
                    expr -> owner != null ? owner.getExpressionTypes().getType(expr) : expr.getType()
            );
            expressionTypes = expressionTypes.merge(typeNarrower.narrowType(branch.getCondition().getExpression()));
        }
    }

    public NodeRT getPredecessor() {
        return owner;
//        if (withBackEdge) {
//            return owner;
//        }
//        if (owner != null) {
//            return owner.getPredecessor();
//        }
//        return null;
    }

    @Nullable
    public Branch getBranch() {
        return branch;
    }

    public @Nullable NodeRT getSuccessor() {
        if (withBackEdge) {
            return owner;
        }
        if (owner != null) {
            return owner.getSuccessor();
        }
        return null;
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

    public @Nullable NodeRT getOwner() {
        return owner;
    }

    public @Nullable ScopeRT getParentScope() {
        return NncUtils.get(owner, NodeRT::getScope);
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

    public boolean isWithBackEdge() {
        return withBackEdge;
    }

    @Nullable
    public NodeRT getLastNode() {
        return nodes.isEmpty() ? null : nodes.get(nodes.size() - 1);
    }

    public ExpressionTypeMap getExpressionTypes() {
        return NncUtils.orElse(expressionTypes, () -> ExpressionTypeMap.EMPTY);
    }

    public void setExpressionTypes(ExpressionTypeMap expressionTypes) {
        this.expressionTypes = expressionTypes;
    }

    public void mergeExpressionTypes(ExpressionTypeMap expressionTypes) {
        this.expressionTypes = getExpressionTypes().merge(expressionTypes);
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

}
