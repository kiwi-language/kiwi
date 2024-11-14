package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.IEntityContext;
import org.metavm.entity.SerializeContext;
import org.metavm.flow.rest.Bytecodes;
import org.metavm.flow.rest.GetPropertyNodeParam;
import org.metavm.flow.rest.NodeDTO;
import org.metavm.object.instance.core.Id;
import org.metavm.object.type.PropertyRef;
import org.metavm.object.type.Type;

import javax.annotation.Nullable;

public class GetPropertyNode extends NodeRT {

    public static GetPropertyNode save(NodeDTO nodeDTO, NodeRT prev, ScopeRT scope, NodeSavingStage stage, IEntityContext context) {
        GetPropertyNode node = (GetPropertyNode) context.getNode(Id.parse(nodeDTO.id()));
        if (node == null) {
            GetPropertyNodeParam param = nodeDTO.getParam();
            var propertyRef = PropertyRef.create(param.propertyRef(), context);
            node = new GetPropertyNode(nodeDTO.tmpId(), nodeDTO.name(),
                    prev, scope, propertyRef);
        }
        return node;
    }

    private final PropertyRef propertyRef;

    public GetPropertyNode(Long tmpId,
                           @NotNull String name,
                           @Nullable NodeRT previous,
                           @NotNull ScopeRT scope,
                           PropertyRef propertyRef) {
        super(tmpId, name, null, previous, scope);
        this.propertyRef = propertyRef;
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitGetFieldNode(this);
    }

    @Override
    protected Object getParam(SerializeContext serializeContext) {
        return new GetPropertyNodeParam(propertyRef.toDTO(serializeContext));
    }

    @NotNull
    public Type getType() {
        return propertyRef.resolve().getType();
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("getProperty " + propertyRef.resolve().getName());
    }

    @Override
    public int getStackChange() {
        return 0;
    }

    @Override
    public void writeCode(CodeOutput output) {
        output.write(Bytecodes.GET_PROPERTY);
        output.writeConstant(propertyRef);
    }

    @Override
    public int getLength() {
        return 3;
    }
}
