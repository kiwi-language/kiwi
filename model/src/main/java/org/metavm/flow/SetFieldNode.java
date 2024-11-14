package org.metavm.flow;

import org.metavm.api.EntityType;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.IEntityContext;
import org.metavm.entity.SerializeContext;
import org.metavm.flow.rest.Bytecodes;
import org.metavm.flow.rest.NodeDTO;
import org.metavm.flow.rest.SetFieldNodeParam;
import org.metavm.object.instance.core.Id;
import org.metavm.object.type.FieldRef;

@EntityType
public class SetFieldNode extends NodeRT {

    public static SetFieldNode save(NodeDTO nodeDTO, NodeRT prev, ScopeRT scope, NodeSavingStage stage, IEntityContext context) {
        SetFieldNodeParam param = nodeDTO.getParam();
        var node = (SetFieldNode) context.getNode(Id.parse(nodeDTO.id()));
        if (node == null) {
            var fieldRef = FieldRef.create(param.fieldRef(), context);
            node = new SetFieldNode(nodeDTO.tmpId(), nodeDTO.name(), prev, scope, fieldRef);
        }
        return node;
    }

    private final FieldRef fieldRef;

    public SetFieldNode(Long tmpId, String name, NodeRT prev, ScopeRT scope, FieldRef fieldRef) {
        super(tmpId, name, null, prev, scope);
        this.fieldRef = fieldRef;
    }
    @Override
    protected SetFieldNodeParam getParam(SerializeContext serializeContext) {
        return new SetFieldNodeParam(fieldRef.toDTO(serializeContext));
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("setField " + fieldRef.getRawField().getName());
    }

    @Override
    public int getStackChange() {
        return -2;
    }

    @Override
    public void writeCode(CodeOutput output) {
        output.write(Bytecodes.SET_FIELD);
        output.writeConstant(fieldRef);
    }

    @Override
    public int getLength() {
        return 3;
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitSetFieldNode(this);
    }
}
