package tech.metavm.flow;

import org.jetbrains.annotations.NotNull;
import tech.metavm.common.ErrorCode;
import tech.metavm.entity.*;
import tech.metavm.expression.FlowParsingContext;
import tech.metavm.flow.rest.GetElementNodeParam;
import tech.metavm.flow.rest.NodeDTO;
import tech.metavm.object.instance.core.ArrayInstance;
import tech.metavm.object.instance.core.Id;
import tech.metavm.object.instance.core.LongInstance;
import tech.metavm.object.type.ArrayType;
import tech.metavm.util.AssertUtils;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;

@EntityType
public class GetElementNode extends NodeRT {

    public static GetElementNode save(NodeDTO nodeDTO, @Nullable NodeRT prev, ScopeRT scope, IEntityContext context) {
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
        var arrayInst = (ArrayInstance) array.evaluate(frame);
        var indexInst = (LongInstance) index.evaluate(frame);
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
