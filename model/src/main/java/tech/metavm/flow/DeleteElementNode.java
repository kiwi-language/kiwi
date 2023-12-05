package tech.metavm.flow;

import org.jetbrains.annotations.Nullable;
import tech.metavm.common.ErrorCode;
import tech.metavm.entity.ChildEntity;
import tech.metavm.entity.EntityType;
import tech.metavm.entity.IEntityContext;
import tech.metavm.entity.ElementVisitor;
import tech.metavm.expression.FlowParsingContext;
import tech.metavm.flow.rest.DeleteElementParam;
import tech.metavm.flow.rest.NodeDTO;
import tech.metavm.object.instance.core.ArrayInstance;
import tech.metavm.object.type.ArrayKind;
import tech.metavm.object.type.ArrayType;
import tech.metavm.entity.StandardTypes;
import tech.metavm.util.AssertUtils;
import tech.metavm.util.BusinessException;
import tech.metavm.util.InstanceUtils;
import tech.metavm.util.NncUtils;

@SuppressWarnings("unused")
@EntityType("删除元素节点")
public class DeleteElementNode extends NodeRT<DeleteElementParam> {

    public static DeleteElementNode create(NodeDTO nodeDTO, @Nullable NodeRT<?> prev, ScopeRT scope, IEntityContext context) {
        var parsingContext = FlowParsingContext.create(scope, prev, context);
        DeleteElementParam param = nodeDTO.getParam();
        var array = ValueFactory.create(param.array(), parsingContext);
        var element = ValueFactory.create(param.element(), parsingContext);
        return new DeleteElementNode(nodeDTO.tmpId(), nodeDTO.name(), prev, scope, array, element);
    }

    @ChildEntity("数组")
    private Value array;
    @ChildEntity("元素")
    private Value element;

    public DeleteElementNode(Long tmpId, String name, NodeRT<?> previous, ScopeRT scope, Value array, Value element) {
        super(tmpId, name, StandardTypes.getBooleanType(), previous, scope);
        check(array, element);
        this.array = addChild(array, "array");
        this.element = addChild(element, "element");
    }

    private void check(Value array, Value element) {
        NncUtils.requireNonNull(array);
        NncUtils.requireNonNull(element);
        if (array.getType() instanceof ArrayType arrayType) {
            AssertUtils.assertTrue(arrayType.getKind() != ArrayKind.READ_ONLY,
                    ErrorCode.MODIFYING_READ_ONLY_ARRAY);
            AssertUtils.assertTrue(arrayType.getElementType().isAssignableFrom(element.getType()),
                    ErrorCode.INCORRECT_ELEMENT_TYPE);
        } else {
            throw new BusinessException(ErrorCode.NOT_AN_ARRAY_VALUE);
        }
    }

    @Override
    protected DeleteElementParam getParam(boolean persisting) {
        return new DeleteElementParam(array.toDTO(persisting), element.toDTO(persisting));
    }

    @Override
    protected void setParam(DeleteElementParam param, IEntityContext context) {
        Value array = this.array;
        Value element = this.element;
        var parsingContext = getParsingContext(context);
        if (param.array() != null) {
            array = ValueFactory.create(param.array(), parsingContext);
        }
        if (param.element() != null) {
            element = ValueFactory.create(param.element(), parsingContext);
        }
        check(array, element);
        this.array = addChild(array, "array");
        this.element = addChild(element, "element");
    }

    public Value getArray() {
        return array;
    }

    public Value getElement() {
        return element;
    }

    @Override
    public NodeExecResult execute(MetaFrame frame) {
        var arrayInst = (ArrayInstance) array.evaluate(frame);
        var elementInst = element.evaluate(frame);
        return next(InstanceUtils.booleanInstance(arrayInst.remove(elementInst)));
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitDeleteElementNode(this);
    }
}
