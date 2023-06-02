package tech.metavm.flow;

import tech.metavm.entity.ChildEntity;
import tech.metavm.entity.EntityField;
import tech.metavm.entity.EntityType;
import tech.metavm.entity.IEntityContext;
import tech.metavm.flow.rest.FieldParamDTO;
import tech.metavm.flow.rest.NodeDTO;
import tech.metavm.flow.rest.SubFlowParam;
import tech.metavm.expression.FlowParsingContext;
import tech.metavm.expression.ParsingContext;
import tech.metavm.object.instance.rest.InstanceDTO;
import tech.metavm.object.meta.ClassType;
import tech.metavm.object.meta.Field;
import tech.metavm.object.meta.Type;
import tech.metavm.object.meta.UnionType;
import tech.metavm.util.InternalException;
import tech.metavm.util.NncUtils;
import tech.metavm.util.Table;

import java.util.List;
import java.util.Map;

@EntityType("子流程节点")
public class SubFlowNode extends NodeRT<SubFlowParam> {

    public static SubFlowNode create(NodeDTO nodeDTO, IEntityContext context) {
        SubFlowParam param = nodeDTO.getParam();
        NodeRT<?> prev = NncUtils.get(nodeDTO.prevId(), context::getNode);
        ScopeRT scope = context.getScope(nodeDTO.scopeId());
        FlowParsingContext flowParsingContext = FlowParsingContext.create(scope, prev, context);
        Value self = ValueFactory.getValue(param.self(), flowParsingContext);
        List<FieldParam> arguments = NncUtils.map(
                param.fields(), p -> new FieldParam(context.getField(p.fieldId()), p.value(), flowParsingContext)
        );
        return new SubFlowNode(nodeDTO, scope, self, arguments, context.getFlow(param.flowId()));
    }

    @ChildEntity("目标对象")
    private Value selfId;
    @ChildEntity("参数列表")
    private final Table<FieldParam> arguments  = new Table<>(FieldParam.class, true);
    @EntityField("子流程")
    private FlowRT subFlow;

    public SubFlowNode(NodeDTO nodeDTO,
                       ScopeRT scope,
                       Value selfId,
                       List<FieldParam> arguments,
                       FlowRT subFlow) {
        super(nodeDTO, subFlow.getOutputType(), scope);
        this.selfId = selfId;
        this.arguments.addAll(arguments);
        this.subFlow = subFlow;
    }

    @Override
    protected void setParam(SubFlowParam param, IEntityContext entityContext) {
        ParsingContext parsingContext = getParsingContext(entityContext);
        selfId = ValueFactory.getValue(param.self(), parsingContext);
        Type type = selfId.getType();
        ClassType selfType = null;
        if(type instanceof ClassType classType) {
            selfType = classType;
        }
        else if((type instanceof UnionType unionType) && unionType.isNullable()) {
            Type underlyingType = unionType.getUnderlyingType();
            if(underlyingType instanceof ClassType classType) {
                selfType = classType;
            }
        }
        if(selfType == null) {
            throw new InternalException("Invalid selfId type '" + type + "'");
        }
        subFlow = selfType.getFlow(param.flowId());
        ClassType inputType = subFlow.getInputType();
        Map<Long, FieldParamDTO> fieldParamMap = NncUtils.toMap(param.fields(), FieldParamDTO::fieldId);
        for (Field field : inputType.getFields()) {
            FieldParamDTO fieldParamDTO = fieldParamMap.get(field.getId());
            if(fieldParamDTO != null) {
                arguments.add(new FieldParam(field, fieldParamDTO.value(), parsingContext));
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
        this.arguments.clear();
        this.arguments.addAll(arguments);
    }

    @Override
    public void execute(FlowFrame frame) {
        FlowStack stack = frame.getStack();
        FlowFrame newFrame = new FlowFrame(
                subFlow,
                selfId.evaluate(frame),
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
