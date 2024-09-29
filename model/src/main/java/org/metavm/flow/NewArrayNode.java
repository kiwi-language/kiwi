package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.api.ChildEntity;
import org.metavm.api.EntityType;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.IEntityContext;
import org.metavm.entity.ReadWriteArray;
import org.metavm.entity.SerializeContext;
import org.metavm.expression.ArrayExpression;
import org.metavm.expression.FlowParsingContext;
import org.metavm.flow.rest.NewArrayNodeParam;
import org.metavm.flow.rest.NodeDTO;
import org.metavm.object.instance.core.ArrayInstance;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.LongValue;
import org.metavm.object.type.ArrayType;
import org.metavm.object.type.TypeParser;
import org.metavm.util.Instances;
import org.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.List;

@EntityType
public class NewArrayNode extends NodeRT implements NewNode {

    public static NewArrayNode save(NodeDTO nodeDTO, NodeRT prev, ScopeRT scope, IEntityContext context) {
        var parsingContext = FlowParsingContext.create(scope, prev, context);
        NewArrayNodeParam param = nodeDTO.getParam();
        var type = (ArrayType) TypeParser.parseType(nodeDTO.outputType(), context);
        var value = NncUtils.get(param.value(), v -> ValueFactory.create(v, parsingContext));
        var dims = param.dimensions() != null ?
                NncUtils.map(param.dimensions(), d -> ValueFactory.create(d, parsingContext)) : null;
        var parentRef = param.parentRef() != null ?
                ParentRef.create(param.parentRef(), parsingContext, context, type) : null;
        NewArrayNode node = (NewArrayNode) context.getNode(Id.parse(nodeDTO.id()));
        if (node != null) {
            node.setValue(value);
            node.setDimensions(dims);
            node.setParentRef(parentRef);
        } else
            node = new NewArrayNode(nodeDTO.tmpId(), nodeDTO.name(), nodeDTO.code(), type, value, dims, parentRef, prev, scope);
        return node;
    }

    @Nullable
    private Value value;

    @Nullable
    @ChildEntity
    private ReadWriteArray<Value> dimensions;

    @Nullable
    private ParentRef parentRef;

    public NewArrayNode(Long tmpId, String name,
                        @Nullable String code,
                        ArrayType type,
                        @Nullable Value value,
                        @Nullable List<Value> dimensions,
                        @Nullable ParentRef parentRef,
                        NodeRT previous,
                        ScopeRT scope) {
        super(tmpId, name, code, type, previous, scope);
        if(value != null && dimensions != null)
            throw new IllegalArgumentException("Value and length can't be present at the same time");
        this.parentRef = parentRef;
        this.value = value;
        this.dimensions = dimensions != null ?
                addChild(new ReadWriteArray<>(Value.class, dimensions), "dimensions") : null;
    }

    @Override
    protected NewArrayNodeParam getParam(SerializeContext serializeContext) {
        return new NewArrayNodeParam(
                NncUtils.get(value, Value::toDTO),
                dimensions != null ? NncUtils.map(dimensions, Value::toDTO) : null,
                NncUtils.get(parentRef, ParentRef::toDTO)
        );
    }

    public void setValue(@Nullable Value value) {
        this.value = value;
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
    public NodeExecResult execute(MetaFrame frame) {
        var instParentRef = NncUtils.get(parentRef, ref -> ref.evaluate(frame));
        // TODO support ephemeral
        var array = new ArrayInstance(getType(), instParentRef);
        if (!array.isChildArray() && value != null)
            array.addAll( value.evaluate(frame).resolveArray());
        else if(dimensions != null) {
            var dims = dimensions.stream().mapToInt(d -> ((LongValue)  d.evaluate(frame)).getValue().intValue()).toArray();
            initArray(array, dims, 0);
        }
        return next(array.getReference());
    }

    private void initArray(ArrayInstance array, int[] dims, int dimOffset) {
        var len = dims[dimOffset];
        if(dimOffset == dims.length - 1) {
            var v = Instances.getDefaultValue(getType().getElementType());
            for (int i = 0; i < len; i++) {
                array.addElement(v);
            }
        } else {
            for (int i = 0; i < len; i++) {
                var subArray = ArrayInstance.allocate((ArrayType) array.getType().getElementType().getUnderlyingType());
                array.addElement(subArray.getReference());
                initArray(subArray, dims, dimOffset + 1);
            }
        }
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("new " + getType().getName() + "(");
        if (value != null)
            writer.write(value.getText());
        else if(dimensions != null)
            writer.write(NncUtils.join(dimensions, d -> "[" + d.getText() + "]", ""));
        writer.write(")");
        if (parentRef != null)
            writer.write(" " + parentRef.getText());
    }

    public @Nullable Value getValue() {
        return value;
    }

    private void setDimensions(@Nullable List<Value> dimensions) {
        if(dimensions == null)
            this.dimensions = null;
        else {
            if(this.dimensions == null)
                this.dimensions = addChild(new ReadWriteArray<>(Value.class, dimensions), "dimensions");
            else
                this.dimensions.reset(dimensions);
        }
    }

    @Override
    public void setParentRef(@Nullable ParentRef parentRef) {
        this.parentRef = parentRef;
    }

    public @Nullable ParentRef getParentRef() {
        return parentRef;
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitNewArrayNode(this);
    }
}
