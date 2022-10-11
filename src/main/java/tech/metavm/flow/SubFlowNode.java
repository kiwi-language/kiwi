package tech.metavm.flow;

import tech.metavm.flow.persistence.NodePO;
import tech.metavm.flow.rest.NodeDTO;
import tech.metavm.flow.rest.SubFlowParam;
import tech.metavm.object.instance.Instance;
import tech.metavm.object.instance.rest.InstanceDTO;
import tech.metavm.util.NncUtils;

import java.util.List;

public class SubFlowNode extends NodeRT<SubFlowParam> {

    private Value selfId;
    private List<FieldParam> arguments;
    private FlowRT flow;

    public SubFlowNode(NodeDTO nodeDTO, SubFlowParam param, ScopeRT scope) {
        super(nodeDTO, scope.getFromContext(FlowRT.class, param.flowId()).getOutputType(), scope);
        setParam(param);
    }

    public SubFlowNode(NodePO nodePO, SubFlowParam param, ScopeRT flow) {
        super(nodePO, flow);
        setParam(param);
    }

    @Override
    protected void setParam(SubFlowParam param) {
        flow = getFlowFromContext(param.flowId());
        selfId = ValueFactory.getValue(param.selfId(), getParsingContext());
        arguments = NncUtils.map(param.fieldParams(), fieldParamDTO -> new FieldParam(fieldParamDTO, context, getParsingContext()));
    }

    @Override
    protected SubFlowParam getParam(boolean persisting) {
        return new SubFlowParam(
                selfId.toDTO(persisting),
                flow.getId(),
                NncUtils.map(arguments, fp -> fp.toDTO(persisting))
        );
    }

    @Override
    public void execute(FlowFrame frame) {
        FlowStack stack = frame.getStack();
        Instance instance = (Instance) selfId.evaluate(frame);
        FlowFrame newContext = new FlowFrame(
                flow, instance, evaluateArguments(frame), stack
        );
        stack.push(newContext);
    }

    private InstanceDTO evaluateArguments(FlowFrame executionContext) {
        return InstanceDTO.valueOf(
                flow.getInputType().getId(),
                NncUtils.map(
                        arguments,
                        fp -> fp.evaluate(executionContext)
                )
        );
    }
}
