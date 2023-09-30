package tech.metavm.flow;

import tech.metavm.entity.IEntityContext;
import tech.metavm.expression.FlowParsingContext;
import tech.metavm.flow.rest.AddElementParamDTO;
import tech.metavm.flow.rest.NodeDTO;
import tech.metavm.object.instance.ArrayInstance;
import tech.metavm.object.instance.Instance;

public class AddElementNode extends NodeRT<AddElementParamDTO>  {

    public static AddElementNode create(NodeDTO nodeDTO, NodeRT<?> prev, ScopeRT scope, IEntityContext context) {
        var parsingContext = FlowParsingContext.create(scope, prev, context);
        AddElementParamDTO param = nodeDTO.getParam();
        var array = ValueFactory.create(param.array(), parsingContext);
        var element = ValueFactory.create(param.element(), parsingContext);
        return new AddElementNode(nodeDTO.tmpId(), nodeDTO.name(), prev, scope, array, element);
    }

    private Value array;
    private Value element;

    protected AddElementNode(Long tmpId, String name, NodeRT<?> previous, ScopeRT scope, Value array, Value element) {
        super(tmpId, name, null, previous, scope);
    }

    @Override
    protected AddElementParamDTO getParam(boolean persisting) {
        return new AddElementParamDTO(array.toDTO(persisting), element.toDTO(persisting));
    }

    @Override
    protected void setParam(AddElementParamDTO param, IEntityContext context) {
        var parsingContext = getParsingContext(context);
        if(param.array() != null) {
            array = ValueFactory.create(param.array(), parsingContext);
        }
        if(param.element() != null) {
            element = ValueFactory.create(param.element(), parsingContext);
        }
    }

    @Override
    public void execute(FlowFrame frame) {
        var arrayInst = (ArrayInstance) array.evaluate(frame);
        var elementInst = (Instance) element.evaluate(frame);
        arrayInst.add(elementInst);
    }
}
