package tech.metavm.flow;

import tech.metavm.entity.ChildEntity;
import tech.metavm.entity.IEntityContext;
import tech.metavm.expression.FlowParsingContext;
import tech.metavm.flow.rest.NewArrayParamDTO;
import tech.metavm.flow.rest.NodeDTO;
import tech.metavm.object.instance.ArrayInstance;
import tech.metavm.object.instance.ArrayType;
import tech.metavm.util.ChildArray;
import tech.metavm.util.NncUtils;

import java.util.List;

public class NewArrayNode extends NodeRT<NewArrayParamDTO> {

    public static NewArrayNode create(NodeDTO nodeDTO, NodeRT<?> prev, ScopeRT scope, IEntityContext context) {
        var parsingContext = FlowParsingContext.create(scope, prev, context);
        NewArrayParamDTO param = nodeDTO.getParam();
        var type = (ArrayType) context.getType(nodeDTO.outputTypeRef());
        var elements = NncUtils.map(param.elements(), e -> ValueFactory.create(e, parsingContext));
        return new NewArrayNode(nodeDTO.tmpId(), nodeDTO.name(), type, elements, prev, scope);
    }

    @ChildEntity("元素")
    private final ChildArray<Value> elements = new ChildArray<>(Value.class);

    public NewArrayNode(Long tmpId, String name, ArrayType type, List<Value> elements, NodeRT<?> previous, ScopeRT scope) {
        super(tmpId, name, type, previous, scope);
        this.elements.addChildren(elements);
    }

    @Override
    protected NewArrayParamDTO getParam(boolean persisting) {
        return new NewArrayParamDTO(
                NncUtils.map(elements, element -> element.toDTO(persisting))
        );
    }

    @Override
    protected void setParam(NewArrayParamDTO param, IEntityContext context) {
        if(param.elements() != null) {
            var parsingContext = getParsingContext(context);
            elements.resetChildren(
                    NncUtils.map(
                            param.elements(),
                            element -> ValueFactory.create(element, parsingContext)
                    )
            );
        }
    }

    @Override
    public ArrayType getType() {
        return (ArrayType) super.getType();
    }

    @Override
    public void execute(MetaFrame frame) {
        frame.setResult(new ArrayInstance(getType(), NncUtils.map(elements, e -> e.evaluate(frame))));
    }

}
