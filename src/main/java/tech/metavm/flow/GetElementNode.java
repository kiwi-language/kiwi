package tech.metavm.flow;

import org.jetbrains.annotations.Nullable;
import tech.metavm.common.ErrorCode;
import tech.metavm.entity.ChildEntity;
import tech.metavm.entity.EntityType;
import tech.metavm.entity.IEntityContext;
import tech.metavm.entity.ElementVisitor;
import tech.metavm.expression.FlowParsingContext;
import tech.metavm.flow.rest.GetElementParam;
import tech.metavm.flow.rest.NodeDTO;
import tech.metavm.object.instance.core.ArrayInstance;
import tech.metavm.object.instance.core.LongInstance;
import tech.metavm.object.type.ArrayType;
import tech.metavm.util.NncUtils;

@SuppressWarnings("unused")
@EntityType("查询元素节点")
public class GetElementNode extends NodeRT<GetElementParam> {

    public static GetElementNode create(NodeDTO nodeDTO, @Nullable NodeRT<?> prev, ScopeRT scope, IEntityContext context) {
        var parsingContext = FlowParsingContext.create(scope, prev, context);
        GetElementParam param = nodeDTO.getParam();
        var array = ValueFactory.create(param.array(), parsingContext);
        var index = ValueFactory.create(param.index(), parsingContext);
        check(array, index);
        return new GetElementNode(nodeDTO.tmpId(), nodeDTO.name(), prev, scope, array, index);
    }

    @ChildEntity("数组")
    private Value array;
    @ChildEntity("索引")
    private Value index;

    public GetElementNode(Long tmpId, String name, NodeRT<?> previous, ScopeRT scope, Value array, Value index) {
        super(tmpId, name, ((ArrayType) array.getType()).getElementType(), previous, scope);
        check(array, index);
        this.array = array;
        this.index = index;
    }

    private static void check(Value array, Value index) {
        NncUtils.requireNonNull(array);
        NncUtils.requireNonNull(index);
        NncUtils.assertTrue(array.getType().isArray(), ErrorCode.NOT_AN_ARRAY_VALUE);
        NncUtils.assertTrue(index.getType().isLong(), ErrorCode.INCORRECT_INDEX_VALUE);
    }

    @Override
    protected GetElementParam getParam(boolean persisting) {
        return new GetElementParam(array.toDTO(persisting), index.toDTO(persisting));
    }

    @Override
    protected void setParam(GetElementParam param, IEntityContext context) {
        Value array = this.array;
        Value index = this.index;
        var parsingContext = getParsingContext(context);
        if (param.array() != null) {
            array = ValueFactory.create(param.array(), parsingContext);
        }
        if (param.index() != null) {
            index = ValueFactory.create(param.index(), parsingContext);
        }
        check(array, index);
        this.array = array;
        this.index = index;
        setOutputType(((ArrayType) array.getType()).getElementType());
    }

    public Value getArray() {
        return array;
    }

    public Value getIndex() {
        return index;
    }

    @Override
    public void execute(MetaFrame frame) {
        var arrayInst = (ArrayInstance) array.evaluate(frame);
        var indexInst = (LongInstance) index.evaluate(frame);
        frame.setResult(arrayInst.get(indexInst.getValue().intValue()));
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitGetElementNode(this);
    }
}
