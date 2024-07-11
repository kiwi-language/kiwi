package org.metavm.flow;

import org.metavm.api.EntityType;
import org.metavm.common.ErrorCode;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.IEntityContext;
import org.metavm.entity.SerializeContext;
import org.metavm.expression.FlowParsingContext;
import org.metavm.flow.rest.NodeDTO;
import org.metavm.flow.rest.RemoveElementNodeParam;
import org.metavm.object.instance.core.Id;
import org.metavm.object.type.ArrayKind;
import org.metavm.object.type.ArrayType;
import org.metavm.object.type.Types;
import org.metavm.util.AssertUtils;
import org.metavm.util.BusinessException;
import org.metavm.util.Instances;
import org.metavm.util.NncUtils;

import javax.annotation.Nullable;

@EntityType
public class RemoveElementNode extends NodeRT {

    public static RemoveElementNode save(NodeDTO nodeDTO, NodeRT prev, ScopeRT scope, IEntityContext context) {
        RemoveElementNodeParam param = nodeDTO.getParam();
        var node = (RemoveElementNode) context.getNode(Id.parse(nodeDTO.id()));
        var parsingContext = FlowParsingContext.create(scope, prev, context);
        var array = ValueFactory.create(param.array(), parsingContext);
        var element = ValueFactory.create(param.element(), parsingContext);
        if (node == null)
            node = new RemoveElementNode(nodeDTO.tmpId(), nodeDTO.name(), nodeDTO.code(), prev, scope, array, element);
        else
            node.update(array, element);
        return node;
    }

    private Value array;
    private Value element;

    public RemoveElementNode(Long tmpId, String name, @Nullable String code, NodeRT previous, ScopeRT scope, Value array, Value element) {
        super(tmpId, name, code, Types.getBooleanType(), previous, scope);
        check(array, element);
        this.array = array;
        this.element = element;
    }

    private void check(Value array, Value element) {
        NncUtils.requireNonNull(array);
        NncUtils.requireNonNull(element);
        if (array.getType() instanceof ArrayType arrayType) {
            AssertUtils.assertTrue(arrayType.getKind() != ArrayKind.READ_ONLY,
                    ErrorCode.MODIFYING_READ_ONLY_ARRAY);
            AssertUtils.assertTrue(arrayType.getElementType().isAssignableFrom(element.getType()),
                    ErrorCode.INCORRECT_ELEMENT_TYPE, arrayType.getElementType().getTypeDesc(), element.getType().getTypeDesc());
        } else {
            throw new BusinessException(ErrorCode.NOT_AN_ARRAY_VALUE);
        }
    }

    @Override
    protected RemoveElementNodeParam getParam(SerializeContext serializeContext) {
        return new RemoveElementNodeParam(array.toDTO(), element.toDTO());
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
