package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.api.EntityType;
import org.metavm.common.ErrorCode;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.IEntityContext;
import org.metavm.entity.SerializeContext;
import org.metavm.expression.FlowParsingContext;
import org.metavm.flow.rest.NodeDTO;
import org.metavm.flow.rest.SetElementNodeParam;
import org.metavm.object.instance.core.LongValue;
import org.metavm.object.type.ArrayKind;
import org.metavm.object.type.ArrayType;
import org.metavm.util.AssertUtils;
import org.metavm.util.BusinessException;

import javax.annotation.Nullable;

@EntityType
public class SetElementNode extends NodeRT {

    public static SetElementNode save(NodeDTO nodeDTO, NodeRT prev, ScopeRT scope, IEntityContext context) {
        var parsingContext = FlowParsingContext.create(scope, prev, context);
        SetElementNodeParam param = nodeDTO.getParam();
        var array = ValueFactory.create(param.array(), parsingContext);
        var index = ValueFactory.create(param.index(), parsingContext);
        var element = ValueFactory.create(param.element(), parsingContext);
        SetElementNode node = (SetElementNode) context.getNode(nodeDTO.id());
        if (node != null)
            node.update(array, index, element);
        else
            node = new SetElementNode(nodeDTO.tmpId(), nodeDTO.name(), nodeDTO.code(), prev, scope, array, index, element);
        return node;
    }

    private Value array;
    private Value index;
    private Value element;

    public SetElementNode(Long tmpId, String name, @Nullable String code, NodeRT previous, ScopeRT scope,
                          Value array, Value index, Value element) {
        super(tmpId, name, code, null, previous, scope);
        check(array, element);
        this.array = array;
        this.index = index;
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
    protected SetElementNodeParam getParam(SerializeContext serializeContext) {
        return new SetElementNodeParam(array.toDTO(), index.toDTO(), element.toDTO());
    }

    public void update(Value array, Value index, Value element) {
        check(array, element);
        this.array = array;
        this.index = index;
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
        var i = ((LongValue) index.evaluate(frame)).getValue().intValue();
        var elementInst = element.evaluate(frame);
        arrayInst.setElement(i, elementInst);
        return next();
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("add(" + array.getText() + ", " + element.getText() + ")");
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitSetElementNode(this);
    }

}
