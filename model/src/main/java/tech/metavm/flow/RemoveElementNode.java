package tech.metavm.flow;

import tech.metavm.common.ErrorCode;
import tech.metavm.entity.*;
import tech.metavm.expression.FlowParsingContext;
import tech.metavm.flow.rest.DeleteElementParam;
import tech.metavm.flow.rest.NodeDTO;
import tech.metavm.flow.rest.RemoveElementNodeParam;
import tech.metavm.object.instance.core.ArrayInstance;
import tech.metavm.object.type.ArrayKind;
import tech.metavm.object.type.ArrayType;
import tech.metavm.util.AssertUtils;
import tech.metavm.util.BusinessException;
import tech.metavm.util.Instances;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;

@EntityType("删除数组元素节点")
public class RemoveElementNode extends NodeRT {

    public static RemoveElementNode save(NodeDTO nodeDTO, NodeRT prev, ScopeRT scope, IEntityContext context) {
        RemoveElementNodeParam param = nodeDTO.getParam();
        var node = (RemoveElementNode) context.getNode(nodeDTO.getRef());
        var parsingContext = FlowParsingContext.create(scope, prev, context);
        var array = ValueFactory.create(param.array(), parsingContext);
        var element = ValueFactory.create(param.element(), parsingContext);
        if (node == null)
            node = new RemoveElementNode(nodeDTO.tmpId(), nodeDTO.name(), nodeDTO.code(), prev, scope, array, element);
        else
            node.update(array, element);
        return node;
    }

    @ChildEntity("数组")
    private Value array;
    @ChildEntity("元素")
    private Value element;

    public RemoveElementNode(Long tmpId, String name, @Nullable String code, NodeRT previous, ScopeRT scope, Value array, Value element) {
        super(tmpId, name, code, StandardTypes.getBooleanType(), previous, scope);
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
    protected DeleteElementParam getParam(SerializeContext serializeContext) {
        return new DeleteElementParam(array.toDTO(), element.toDTO());
    }

    public void update(Value array, Value element) {
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
        return next(Instances.booleanInstance(arrayInst.removeElement(elementInst)));
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("remove(" + array.getText() + "," + element.getText() + ")");
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitDeleteElementNode(this);
    }
}
