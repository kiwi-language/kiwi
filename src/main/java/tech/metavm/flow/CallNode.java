package tech.metavm.flow;

import tech.metavm.dto.RefDTO;
import tech.metavm.entity.ChildEntity;
import tech.metavm.entity.EntityField;
import tech.metavm.entity.EntityType;
import tech.metavm.entity.IEntityContext;
import tech.metavm.entity.natives.NativeInvoker;
import tech.metavm.expression.ParsingContext;
import tech.metavm.flow.rest.CallParamDTO;
import tech.metavm.flow.rest.FieldParamDTO;
import tech.metavm.flow.rest.ValueDTO;
import tech.metavm.object.instance.ClassInstance;
import tech.metavm.object.instance.Instance;
import tech.metavm.object.meta.ClassType;
import tech.metavm.object.meta.Field;
import tech.metavm.util.NncUtils;
import tech.metavm.util.Table;

import java.util.Collections;
import java.util.List;

@EntityType("Call节点")
public abstract class CallNode<T extends CallParamDTO> extends NodeRT<T> {

    public static Flow getFlow(CallParamDTO param, IEntityContext context) {
        return context.getEntity(Flow.class, param.getFlowRef());
    }

    @ChildEntity("参数列表")
    protected final Table<FieldParam> arguments = new Table<>(FieldParam.class, true);
    @EntityField("子流程")
    protected Flow subFlow;

    public CallNode(Long tmpId, String name, NodeRT<?> prev, ScopeRT scope, List<FieldParam> arguments,
                    Flow subFlow) {
        super(tmpId, name, subFlow.getOutputType(), prev, scope);
        this.arguments.addAll(arguments);
        this.subFlow = subFlow;
    }

    @Override
    protected void setParam(T param, IEntityContext context) {
        setCallParam(param, context);
    }

    protected void setCallParam(CallParamDTO param, IEntityContext context) {
        ParsingContext parsingContext = getParsingContext(context);
        subFlow = context.getFlow(param.getFlowRef());
        ClassType inputType = subFlow.getInputType();
        if (param.getFields() != null) {
            for (FieldParamDTO(RefDTO fieldRef, ValueDTO value) : param.getFields()) {
                Field field = context.getField(fieldRef);
                NncUtils.requireTrue(field.getDeclaringType() == inputType);
                arguments.add(new FieldParam(field, value, parsingContext));
            }
        }
    }

    @Override
    protected abstract T getParam(boolean persisting);

    public Flow getSubFlow() {
        return subFlow;
    }

    public void setSubFlow(Flow flow) {
        this.subFlow = flow;
    }

    public void setArguments(List<FieldParam> arguments) {
        this.arguments.clear();
        this.arguments.addAll(arguments);
    }

    public List<FieldParam> getArguments() {
        return Collections.unmodifiableList(arguments);
    }

    protected abstract Instance getSelf(FlowFrame frame);

    @Override
    public void execute(FlowFrame frame) {
        FlowStack stack = frame.getStack();
        var self = getSelf(frame);
        var argument = (ClassInstance) evaluateArguments(frame);
        var flow = subFlow;
        if (flow.isAbstract()) {
            flow = ((ClassType) self.getType()).getOverrideFlowRequired(flow);
        }
        if (flow.isNative()) {
            var result = NativeInvoker.invoke(flow, self, argument);
            if (result != null) {
                frame.setResult(result);
            }
        } else {
            FlowFrame newFrame = new FlowFrame(flow, self, argument, stack);
            stack.push(newFrame);
        }
    }

    private Instance evaluateArguments(FlowFrame executionContext) {
        return new ClassInstance(
                NncUtils.toMap(
                        arguments,
                        FieldParam::getField,
                        arg -> arg.evaluate(executionContext)
                ),
                subFlow.getInputType()
        );
    }

}
