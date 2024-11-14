package org.metavm.flow;

import org.metavm.api.EntityType;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.IEntityContext;
import org.metavm.entity.LoadAware;
import org.metavm.entity.SerializeContext;
import org.metavm.flow.rest.Bytecodes;
import org.metavm.flow.rest.NodeDTO;
import org.metavm.object.instance.core.Id;

import java.util.Objects;

@EntityType
public class TryEnterNode extends NodeRT implements LoadAware {

    public static TryEnterNode save(NodeDTO nodeDTO, NodeRT prev, ScopeRT scope, NodeSavingStage stage, IEntityContext context) {
        var node = (TryEnterNode) context.getNode(Id.parse(nodeDTO.id()));
        if (node == null)
            node = new TryEnterNode(nodeDTO.tmpId(), nodeDTO.name(), prev, scope);
        return node;
    }

    private transient TryExitNode exit;

    public TryEnterNode(Long tmpId, String name, NodeRT previous, ScopeRT scope) {
        super(tmpId, name, null, previous, scope);
    }

    @Override
    protected Object getParam(SerializeContext serializeContext) {
        return null;
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("try-enter");
    }

    @Override
    public int getStackChange() {
        return 0;
    }

    @Override
    public void writeCode(CodeOutput output) {
        output.write(Bytecodes.TRY_ENTER);
        output.writeShort(exit.getOffset());
    }

    @Override
    public int getLength() {
        return 3;
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitTryEnterNode(this);
    }

    public TryExitNode getExit() {
        if(exit != null)
            return exit;
        int numEntries = 0;
        for(var n = getSuccessor(); n != null; n = n.getSuccessor()) {
            if(n instanceof TryEnterNode)
                numEntries++;
            else if(n instanceof TryExitNode e) {
                if(numEntries == 0) {
                    exit = e;
                    break;
                }
                numEntries--;
            }
        }
        return Objects.requireNonNull(exit, () -> "Cannot find exit for TryEnterNode " + getName());
    }

    @Override
    public void onLoad() {
        getExit();
    }

    void setExit(TryExitNode exit) {
        this.exit = exit;
    }
}
