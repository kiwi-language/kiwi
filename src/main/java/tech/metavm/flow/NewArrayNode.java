package tech.metavm.flow;

import org.jetbrains.annotations.NotNull;
import tech.metavm.entity.ChildEntity;
import tech.metavm.entity.IEntityContext;
import tech.metavm.expression.ArrayExpression;
import tech.metavm.entity.ElementVisitor;
import tech.metavm.expression.FlowParsingContext;
import tech.metavm.flow.rest.NewArrayParam;
import tech.metavm.flow.rest.NodeDTO;
import tech.metavm.object.instance.core.ArrayInstance;
import tech.metavm.object.type.ArrayType;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;

public class NewArrayNode extends NodeRT<NewArrayParam> implements NewNode {

    public static NewArrayNode create(NodeDTO nodeDTO, NodeRT<?> prev, ScopeRT scope, IEntityContext context) {
        var parsingContext = FlowParsingContext.create(scope, prev, context);
        NewArrayParam param = nodeDTO.getParam();
        var type = (ArrayType) context.getType(nodeDTO.outputTypeRef());
        var value = ValueFactory.create(param.value(), parsingContext);
        var parentRef = param.parentRef() != null ?
                ParentRef.create(param.parentRef(), parsingContext, type) : null;
        return new NewArrayNode(nodeDTO.tmpId(), nodeDTO.name(), type, value, parentRef, prev, scope);
    }

    @ChildEntity("值")
    private Value value;

    @ChildEntity("父对象")
    @Nullable
    private ParentRef parentRef;


    public NewArrayNode(Long tmpId, String name,
                        ArrayType type,
                        Value value,
                        @Nullable ParentRef parentRef,
                        NodeRT<?> previous,
                        ScopeRT scope) {
        super(tmpId, name, type, previous, scope);
        setParent(parentRef);
        this.value = addChild(check(value), "value");
    }

    @Override
    protected NewArrayParam getParam(boolean persisting) {
        return new NewArrayParam(
                value.toDTO(persisting),
                NncUtils.get(parentRef, ParentRef::toDTO)
        );
    }

    @Override
    protected void setParam(NewArrayParam param, IEntityContext context) {
        var parsingContext = getParsingContext(context);
        if (param.value() != null) {
            this.value = addChild(check(ValueFactory.create(param.value(), parsingContext)), "value");
        }
        if (param.parentRef() != null) {
            setParent(ParentRef.create(param.parentRef(), parsingContext, getType()));
        } else {
            setParent(null);
        }
    }

    private Value check(Value value) {
        if (getType().isChildArray()) {
            var valueExpr = value.getExpression();
            NncUtils.requireTrue(valueExpr instanceof ArrayExpression arrayExpr
                    && arrayExpr.getExpressions().isEmpty());
        } else {
            NncUtils.requireTrue(value.getType() instanceof ArrayType);
            var valueType = (ArrayType) value.getType();
            NncUtils.requireTrue(getType().getElementType().isAssignableFrom(valueType.getElementType()));
        }
        return value;
    }

    @Override
    @NotNull
    public ArrayType getType() {
        return (ArrayType) NncUtils.requireNonNull(super.getType());
    }

    @Override
    public void execute(MetaFrame frame) {
        var instParentRef = NncUtils.get(parentRef, ref -> ref.evaluate(frame));
        var array = new ArrayInstance(getType(), instParentRef);
        if (!array.isChildArray()) {
            array.addAll((ArrayInstance) value.evaluate(frame));
        }
        frame.setResult(array);
    }

    public Value getValue() {
        return value;
    }

    @Override
    public void setParent(@Nullable ParentRef parentRef) {
        this.parentRef = NncUtils.get(parentRef, p -> addChild(p, "parentRef"));
    }

    public @Nullable ParentRef getParentRef() {
        return parentRef;
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitNewArrayNode(this);
    }
}
