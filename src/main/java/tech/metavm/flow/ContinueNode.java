package tech.metavm.flow;

import org.jetbrains.annotations.Nullable;
import tech.metavm.entity.IEntityContext;
import tech.metavm.flow.rest.ContinueParamDTO;
import tech.metavm.object.meta.Type;

public class ContinueNode extends NodeRT<ContinueParamDTO>  {

    protected ContinueNode(Long tmpId, String name, @Nullable Type outputType, NodeRT<?> previous, ScopeRT scope) {
        super(tmpId, name, outputType, previous, scope);
    }

    @Override
    protected ContinueParamDTO getParam(boolean persisting) {
        return null;
    }

    @Override
    protected void setParam(ContinueParamDTO param, IEntityContext context) {

    }

    @Override
    public void execute(FlowFrame frame) {

    }
}
