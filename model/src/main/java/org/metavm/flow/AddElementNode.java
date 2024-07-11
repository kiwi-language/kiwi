package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.api.EntityType;
import org.metavm.common.ErrorCode;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.IEntityContext;
import org.metavm.entity.SerializeContext;
import org.metavm.expression.FlowParsingContext;
import org.metavm.flow.rest.AddElementNodeParam;
import org.metavm.flow.rest.NodeDTO;
import org.metavm.object.type.ArrayKind;
import org.metavm.object.type.ArrayType;
import org.metavm.util.AssertUtils;
import org.metavm.util.BusinessException;

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
        var arrayInst = array.evaluate(frame).resolveArray();
        var elementInst = element.evaluate(frame);
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
