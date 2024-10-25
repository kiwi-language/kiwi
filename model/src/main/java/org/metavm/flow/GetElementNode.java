package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.api.EntityType;
import org.metavm.common.ErrorCode;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.IEntityContext;
import org.metavm.entity.SerializeContext;
import org.metavm.expression.FlowParsingContext;
import org.metavm.flow.rest.GetElementNodeParam;
import org.metavm.flow.rest.NodeDTO;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.LongValue;
import org.metavm.object.type.ArrayType;
import org.metavm.util.AssertUtils;
import org.metavm.util.NncUtils;

import javax.annotation.Nullable;

@EntityType
public class GetElementNode extends NodeRT {

    public static GetElementNode save(NodeDTO nodeDTO, @Nullable NodeRT prev, ScopeRT scope, NodeSavingStage stage, IEntityContext context) {
        var parsingContext = FlowParsingContext.create(scope, prev, context);
        GetElementNodeParam param = nodeDTO.getParam();
        var array = ValueFactory.create(param.array(), parsingContext);
        var index = ValueFactory.create(param.index(), parsingContext);
        GetElementNode node = (GetElementNode) context.getNode(Id.parse(nodeDTO.id()));
        if (node != null) {
            node.setArray(array);
            node.setIndex(index);
        } else
            node = new GetElementNode(nodeDTO.tmpId(), nodeDTO.name(), nodeDTO.code(), prev, scope, array, index);
        return node;
    }

    private Value array;
    private Value index;

    public GetElementNode(Long tmpId, String name, @javax.annotation.Nullable String code, NodeRT previous, ScopeRT scope, Value array, Value index) {
        super(tmpId, name, code, ((ArrayType) array.getType()).getElementType(), previous, scope);
        check(array, index);
        this.array = array;
        this.index = index;
    }

    private static void check(Value array, Value index) {
        NncUtils.requireNonNull(array);
        NncUtils.requireNonNull(index);
        AssertUtils.assertTrue(array.getType().isArray(), ErrorCode.NOT_AN_ARRAY_VALUE);
        AssertUtils.assertTrue(index.getType().isLong(), ErrorCode.INCORRECT_INDEX_VALUE);
    }

    @Override
    protected GetElementNodeParam getParam(SerializeContext serializeContext) {
        return new GetElementNodeParam(array.toDTO(), index.toDTO());
    }

    public void setIndex(@NotNull Value index) {
        AssertUtils.assertTrue(index.getType().isLong(), ErrorCode.INCORRECT_INDEX_VALUE);
        this.index = index;
    }

    public void setArray(@NotNull Value array) {
        AssertUtils.assertTrue(array.getType().isArray(), ErrorCode.NOT_AN_ARRAY_VALUE);
        this.array = array;
        setOutputType(((ArrayType) array.getType()).getElementType());
    }

    public Value getArray() {
        return array;
    }

    public Value getIndex() {
        return index;
    }

    @Override
    public NodeExecResult execute(MetaFrame frame) {
        var arrayInst = array.evaluate(frame).resolveArray();
        var indexInst = (LongValue) index.evaluate(frame);
        return next(arrayInst.get(indexInst.getValue().intValue()));
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write(array.getText() + "[" + index.getText() + "]");
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitGetElementNode(this);
    }
}
