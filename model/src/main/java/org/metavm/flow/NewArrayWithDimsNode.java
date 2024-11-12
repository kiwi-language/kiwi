package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.api.EntityType;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.IEntityContext;
import org.metavm.entity.SerializeContext;
import org.metavm.flow.rest.NewArrayWithDimsNodeParam;
import org.metavm.flow.rest.NodeDTO;
import org.metavm.object.instance.core.ArrayInstance;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.LongValue;
import org.metavm.object.type.ArrayType;
import org.metavm.object.type.TypeParser;
import org.metavm.util.Instances;
import org.metavm.util.NncUtils;

import javax.annotation.Nullable;

@EntityType
public class NewArrayWithDimsNode extends NodeRT {

    public static NewArrayWithDimsNode save(NodeDTO nodeDTO, NodeRT prev, ScopeRT scope, NodeSavingStage stage, IEntityContext context) {
        var param = (NewArrayWithDimsNodeParam) nodeDTO.getParam();
        var type = (ArrayType) TypeParser.parseType(nodeDTO.outputType(), context);
        var dims = param.dimensions();
        NewArrayWithDimsNode node = (NewArrayWithDimsNode) context.getNode(Id.parse(nodeDTO.id()));
        if (node == null)
            node = new NewArrayWithDimsNode(nodeDTO.tmpId(), nodeDTO.name(), type, prev, scope, dims);
        return node;
    }

    private final int dimensions;

    public NewArrayWithDimsNode(Long tmpId, String name,
                                ArrayType type,
                                NodeRT previous,
                                ScopeRT scope,
                                int dimensions) {
        super(tmpId, name, type, previous, scope);
        this.dimensions = dimensions;
    }

    @Override
    protected Object getParam(SerializeContext serializeContext) {
        return new NewArrayWithDimsNodeParam(dimensions);
    }

    @Override
    @NotNull
    public ArrayType getType() {
        return (ArrayType) NncUtils.requireNonNull(super.getType());
    }

    @Override
    public int execute(MetaFrame frame) {
        var array = new ArrayInstance(getType());
        var dims = new int[dimensions];
        for (int i = dimensions - 1; i >= 0; i--) {
            dims[i] = ((LongValue)frame.pop()).getValue().intValue();
        }
        Instances.initArray(array, dims, 0);
        frame.push(array.getReference());
        return MetaFrame.STATE_NEXT;
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("new " + getType().getName() + " dimensions = " + dimensions);
    }

    @Override
    public int getStackChange() {
        return 1 - dimensions;
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitNewArrayWithDimsNode(this);
    }
}
