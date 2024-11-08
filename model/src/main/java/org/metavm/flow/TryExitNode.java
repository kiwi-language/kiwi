package org.metavm.flow;

import org.metavm.api.EntityType;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.IEntityContext;
import org.metavm.entity.LoadAware;
import org.metavm.entity.SerializeContext;
import org.metavm.flow.rest.NodeDTO;
import org.metavm.flow.rest.TryExitNodeParam;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.Value;
import org.metavm.util.Instances;

import javax.annotation.Nullable;

@EntityType
public class TryExitNode extends NodeRT implements LoadAware {

    public static TryExitNode save(NodeDTO nodeDTO, NodeRT prev, ScopeRT scope, NodeSavingStage stage, IEntityContext context) {
        var node = (TryExitNode) context.getNode(Id.parse(nodeDTO.id()));
        if (node == null) {
            var param = (TryExitNodeParam) nodeDTO.param();
            node = new TryExitNode(nodeDTO.tmpId(), nodeDTO.name(), nodeDTO.code(), prev, scope, param.variableIndex());
        }
        return node;
    }

    private final int variableIndex;
    private transient TryEnterNode entry;

    public TryExitNode(Long tmpId, String name, @Nullable String code, NodeRT previous, ScopeRT scope, int variableIndex) {
        super(tmpId, name, code, null, previous, scope);
        findEntry();
        this.variableIndex = variableIndex;
    }

    private void findEntry() {
        int numExits = 0;
        for(var n = getPredecessor(); n != null; n = n.getPredecessor()) {
            if(n instanceof TryEnterNode t) {
                if(numExits == 0) {
                    this.entry = t;
                    return;
                }
                numExits--;
            }
            else if(n instanceof TryExitNode)
                numExits++;
        }
        throw new IllegalStateException("Cannot find the matching entry node for TryExitNode " + getName());
    }

    @Override
    protected TryExitNodeParam getParam(SerializeContext serializeContext) {
        return new TryExitNodeParam(variableIndex);
    }

    @Override
    public NodeExecResult execute(MetaFrame frame) {
        var tryNode = frame.exitTrySection();
        assert tryNode == entry;
        var exceptionInfo = frame.getExceptionInfo(entry);
        Value exception;
        if (exceptionInfo != null)
            exception = exceptionInfo.exception().getReference();
        else
            exception = Instances.nullInstance();
        frame.store(variableIndex, exception);
        return next();
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("try-exit variable = " + variableIndex);
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitTryExitNode(this);
    }

    @Override
    public void onLoad() {
        findEntry();
    }

    public int getVariableIndex() {
        return variableIndex;
    }
}
