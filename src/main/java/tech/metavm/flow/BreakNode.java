package tech.metavm.flow;

import org.jetbrains.annotations.Nullable;
import tech.metavm.entity.IEntityContext;
import tech.metavm.flow.rest.BreakParamDTO;
import tech.metavm.object.meta.Type;

public class BreakNode extends NodeRT<BreakParamDTO> {


    protected BreakNode(Long tmpId, String name, @Nullable Type outputType, NodeRT<?> previous, ScopeRT scope) {
        super(tmpId, name, outputType, previous, scope);
    }

    @Override
    protected BreakParamDTO getParam(boolean persisting) {
        return null;
    }

    @Override
    protected void setParam(BreakParamDTO param, IEntityContext context) {

    }

    @Override
    public void execute(FlowFrame frame) {

    }
}
