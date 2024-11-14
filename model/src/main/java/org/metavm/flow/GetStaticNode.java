package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.IEntityContext;
import org.metavm.entity.SerializeContext;
import org.metavm.flow.rest.Bytecodes;
import org.metavm.flow.rest.GetStaticNodeParam;
import org.metavm.flow.rest.NodeDTO;
import org.metavm.object.instance.core.Id;
import org.metavm.object.type.PropertyRef;
import org.metavm.object.type.Type;

import javax.annotation.Nullable;

public class GetStaticNode extends NodeRT {

    public static GetStaticNode save(NodeDTO nodeDTO, NodeRT prev, ScopeRT scope, NodeSavingStage stage, IEntityContext context) {
        GetStaticNode node = (GetStaticNode) context.getNode(Id.parse(nodeDTO.id()));
        if (node == null) {
            GetStaticNodeParam param = nodeDTO.getParam();
            var propertyRef = PropertyRef.create(param.propertyRef(), context);
            node = new GetStaticNode(nodeDTO.tmpId(), nodeDTO.name(),
                    prev, scope, propertyRef);
        }
        return node;
    }

    private final PropertyRef propertyRef;

    public GetStaticNode(Long tmpId,
                         @NotNull String name,
                         @Nullable NodeRT previous,
                         @NotNull ScopeRT scope,
                         PropertyRef propertyRef) {
        super(tmpId, name, null, previous, scope);
        this.propertyRef = propertyRef;
    }

    @NotNull
    @Override
    public Type getType() {
        return propertyRef.resolve().getType();
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitGetStaticNode(this);
    }

    @Override
    protected Object getParam(SerializeContext serializeContext) {
        return new GetStaticNodeParam( propertyRef.toDTO(serializeContext));
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write(propertyRef.resolve().getQualifiedName());
    }

    @Override
    public int getStackChange() {
        return 1;
    }

    @Override
    public void writeCode(CodeOutput output) {
        output.write(Bytecodes.GET_STATIC);
        output.writeConstant(propertyRef);
    }

    @Override
    public int getLength() {
        return 3;
    }

}
