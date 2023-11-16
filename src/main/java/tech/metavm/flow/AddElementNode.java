package tech.metavm.flow;

import tech.metavm.common.ErrorCode;
import tech.metavm.entity.*;
import tech.metavm.expression.FlowParsingContext;
import tech.metavm.flow.rest.AddElementParamDTO;
import tech.metavm.flow.rest.NodeDTO;
import tech.metavm.object.instance.core.ArrayInstance;
import tech.metavm.object.instance.core.Instance;
import tech.metavm.object.type.ArrayKind;
import tech.metavm.object.type.ArrayType;
import tech.metavm.util.BusinessException;
import tech.metavm.util.NncUtils;

@EntityType("添加元素节点")
public class AddElementNode extends NodeRT<AddElementParamDTO> {

    public static AddElementNode create(NodeDTO nodeDTO, NodeRT<?> prev, ScopeRT scope, IEntityContext context) {
        var parsingContext = FlowParsingContext.create(scope, prev, context);
        AddElementParamDTO param = nodeDTO.getParam();
        var array = ValueFactory.create(param.array(), parsingContext);
        var element = ValueFactory.create(param.element(), parsingContext);
        return new AddElementNode(nodeDTO.tmpId(), nodeDTO.name(), prev, scope, array, element);
    }

    @ChildEntity("数组")
    private Value array;
    @ChildEntity("元素")
    private Value element;

    public AddElementNode(Long tmpId, String name, NodeRT<?> previous, ScopeRT scope, Value array, Value element) {
        super(tmpId, name, null, previous, scope);
        check(array, element);
        this.array = array;
        this.element = element;
    }

    private void check(Value array, Value element) {
        NncUtils.requireNonNull(array);
        NncUtils.requireNonNull(element);
        if (array.getType() instanceof ArrayType arrayType) {
            NncUtils.assertTrue(arrayType.getKind() == ArrayKind.READ_WRITE,
                    ErrorCode.ADD_ELEMENT_NOT_SUPPORTED);
            NncUtils.assertTrue(arrayType.getElementType().isAssignableFrom(element.getType()),
                    ErrorCode.INCORRECT_ELEMENT_TYPE);
        } else {
            throw new BusinessException(ErrorCode.NOT_AN_ARRAY_VALUE);
        }
    }

    @Override
    protected AddElementParamDTO getParam(boolean persisting) {
        return new AddElementParamDTO(array.toDTO(persisting), element.toDTO(persisting));
    }

    @Override
    protected void setParam(AddElementParamDTO param, IEntityContext context) {
        var parsingContext = getParsingContext(context);
        var array = this.array;
        var element = this.element;
        if (param.array() != null) {
            array = ValueFactory.create(param.array(), parsingContext);
        }
        if (param.element() != null) {
            element = ValueFactory.create(param.element(), parsingContext);
        }
        check(array, element);
        this.array = array;
        this.element = element;
    }

    public Value getArray() {
        return array;
    }

    public Value getElement() {
        return element;
    }

    @Override
    public void execute(MetaFrame frame) {
        var arrayInst = (ArrayInstance) array.evaluate(frame);
        var elementInst = (Instance) element.evaluate(frame);
        arrayInst.add(elementInst);
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitAddElementNode(this);
    }

}
