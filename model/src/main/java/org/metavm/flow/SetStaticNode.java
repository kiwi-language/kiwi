package org.metavm.flow;

import org.metavm.api.EntityType;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.IEntityContext;
import org.metavm.entity.SerializeContext;
import org.metavm.flow.rest.Bytecodes;
import org.metavm.flow.rest.NodeDTO;
import org.metavm.flow.rest.SetStaticNodeParam;
import org.metavm.object.instance.core.Id;
import org.metavm.object.type.FieldRef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@EntityType
public class SetStaticNode extends NodeRT {

    public static final Logger logger = LoggerFactory.getLogger(SetStaticNode.class);

    public static SetStaticNode save(NodeDTO nodeDTO, NodeRT prev, Code code, NodeSavingStage stage, IEntityContext context) {
        SetStaticNodeParam param = nodeDTO.getParam();
        var node = (SetStaticNode) context.getNode(Id.parse(nodeDTO.id()));
        if (node == null) {
            var fieldRef = FieldRef.create(param.fieldRef(), context);
            node = new SetStaticNode(nodeDTO.tmpId(), nodeDTO.name(), prev, code,  fieldRef);
        }
        return node;
    }

    private final FieldRef fieldRef;

    public SetStaticNode(Long tmpId, String name, NodeRT previous, Code code, FieldRef fieldRef) {
        super(tmpId, name, null, previous, code);
        this.fieldRef = fieldRef;
    }

    @Override
    protected SetStaticNodeParam getParam(SerializeContext serializeContext) {
        return new SetStaticNodeParam(fieldRef.toDTO(serializeContext));
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("setStatic " + fieldRef.resolve().getQualifiedName());
    }

    @Override
    public int getStackChange() {
        return -1;
    }

    @Override
    public void writeCode(CodeOutput output) {
        output.write(Bytecodes.SET_STATIC);
        output.writeConstant(fieldRef);
    }

    @Override
    public int getLength() {
        return 3;
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitSetStaticNode(this);
    }
}
