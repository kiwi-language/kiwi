package tech.metavm.flow;

import org.jetbrains.annotations.NotNull;
import tech.metavm.common.ErrorCode;
import tech.metavm.entity.*;
import tech.metavm.expression.FlowParsingContext;
import tech.metavm.flow.rest.AddElementNodeParam;
import tech.metavm.flow.rest.NodeDTO;
import tech.metavm.object.instance.core.ArrayInstance;
import tech.metavm.object.instance.core.Instance;
import tech.metavm.object.type.ArrayKind;
import tech.metavm.object.type.ArrayType;
import tech.metavm.util.AssertUtils;
import tech.metavm.util.BusinessException;

import javax.annotation.Nullable;

@EntityType
public class AddElementNode extends NodeRT {

    public static AddElementNode save(NodeDTO nodeDTO, NodeRT prev, ScopeRT scope, IEntityContext context) {
        var parsingContext = FlowParsingContext.create(scope, prev, context);
        AddElementNodeParam param = nodeDTO.getParam();
        var array = ValueFactory.create(param.array(), parsingContext);
        var element = ValueFactory.create(param.element(), parsingContext);
        AddElementNode node = (AddElementNode) context.getNode(nodeDTO.id());
        if (node != null)
            node.update(array, element);
        else
            node = new AddElementNode(nodeDTO.tmpId(), nodeDTO.name(), nodeDTO.code(), prev, scope, array, element);
        return node;
    }

    private Value array;
    private Value element;

    public AddElementNode(Long tmpId, String name, @Nullable String code,  NodeRT previous, ScopeRT scope, Value array, Value element) {
        super(tmpId, name, code, null, previous, scope);
        check(array, element);
        this.array = array;
        this.element = element;
    }

    private void check(@NotNull Value array, @NotNull Value element) {
        if (array.getType() instanceof ArrayType arrayType) {
            AssertUtils.assertTrue(arrayType.getKind() != ArrayKind.READ_ONLY,
                    ErrorCode.ADD_ELEMENT_NOT_SUPPORTED);
            AssertUtils.assertTrue(arrayType.getElementType().isAssignableFrom(element.getType()),
                    ErrorCode.INCORRECT_ELEMENT_TYPE, arrayType.getElementType().getTypeDesc(), element.getType().getTypeDesc());
        } else {
            throw new BusinessException(ErrorCode.NOT_AN_ARRAY_VALUE);
        }
    }

    @Override
    protected AddElementNodeParam getParam(SerializeContext serializeContext) {
        return new AddElementNodeParam(array.toDTO(), element.toDTO());
    }

    public void update(Value array, Value element) {
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
    public NodeExecResult execute(MetaFrame frame) {
        var arrayInst = (ArrayInstance) array.evaluate(frame);
        var elementInst = (Instance) element.evaluate(frame);
        arrayInst.addElement(elementInst);
        return next();
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("add(" + array.getText() + ", " + element.getText() + ")");
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitAddElementNode(this);
    }

}
