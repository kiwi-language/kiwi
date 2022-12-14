package tech.metavm.flow;

import tech.metavm.entity.EntityField;
import tech.metavm.entity.EntityType;
import tech.metavm.flow.rest.FieldParamDTO;
import tech.metavm.flow.rest.NodeDTO;
import tech.metavm.flow.rest.SubFlowParam;
import tech.metavm.object.instance.Instance;
import tech.metavm.object.instance.rest.InstanceDTO;
import tech.metavm.object.meta.ClassType;
import tech.metavm.object.meta.Field;
import tech.metavm.util.NncUtils;
import tech.metavm.util.Table;

import java.util.List;
import java.util.Map;

@EntityType("子流程节点")
public class SubFlowNode extends NodeRT<SubFlowParam> {

    @EntityField("目标对象")
    private Value selfId;
    @EntityField("参数列表")
    private Table<FieldParam> arguments;
    @EntityField("子流程")
    private FlowRT subFlow;

    public SubFlowNode(NodeDTO nodeDTO,
                       ScopeRT scope,
                       Value selfId,
                       List<FieldParam> arguments,
                       FlowRT subFlow) {
        super(nodeDTO, subFlow.getOutputType(), scope);
        this.selfId = selfId;
        this.arguments = new Table<>(FieldParam.class, arguments);
        this.subFlow = subFlow;
    }

    @Override
    protected void setParam(SubFlowParam param) {
        selfId = ValueFactory.getValue(param.self(), getParsingContext());
        ClassType selfType = (ClassType) selfId.getType();
        subFlow = selfType.getFlow(param.flowId());
        Map<Long, FieldParamDTO> fieldParamMap = NncUtils.toMap(param.fields(), FieldParamDTO::fieldId);
        arguments = new Table<>(FieldParam.class);
        for (Field field : selfType.getFields()) {
            FieldParamDTO fieldParamDTO = fieldParamMap.get(field.getId());
            if(fieldParamDTO != null) {
                arguments.add(new FieldParam(field, fieldParamDTO.value(), getParsingContext()));
            }
        }
    }

    @Override
    protected SubFlowParam getParam(boolean persisting) {
        return new SubFlowParam(
                selfId.toDTO(persisting),
                subFlow.getId(),
                NncUtils.map(arguments, fp -> fp.toDTO(persisting))
        );
    }

    public Value getSelfId() {
        return selfId;
    }

    public FlowRT getSubFlow() {
        return subFlow;
    }

    public void setSelfId(Value selfId) {
        this.selfId = selfId;
    }

    public void setSubFlow(FlowRT flow) {
        this.subFlow = flow;
    }

    public void setArguments(List<FieldParam> arguments) {
        this.arguments = new Table<>(FieldParam.class, arguments);
    }

    @Override
    public void execute(FlowFrame frame) {
        FlowStack stack = frame.getStack();
        FlowFrame newFrame = new FlowFrame(
                subFlow,
                (Instance) selfId.evaluate(frame),
                evaluateArguments(frame),
                stack
        );
        stack.push(newFrame);
    }

    private InstanceDTO evaluateArguments(FlowFrame executionContext) {
        return InstanceDTO.valueOf(
                subFlow.getInputType().getId(),
                NncUtils.map(
                        arguments,
                        fp -> fp.evaluate(executionContext)
                )
        );
    }
}
