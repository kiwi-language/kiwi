package org.metavm.flow;

import org.metavm.api.EntityType;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.IEntityContext;
import org.metavm.entity.SerializeContext;
import org.metavm.expression.FlowParsingContext;
import org.metavm.flow.rest.CopyNodeParam;
import org.metavm.flow.rest.NodeDTO;
import org.metavm.object.instance.core.Id;
import org.metavm.object.type.FieldRef;
import org.metavm.util.NncUtils;

import javax.annotation.Nullable;

@EntityType
public class CopyNode extends NodeRT {

    public static CopyNode save(NodeDTO nodeDTO, NodeRT prev, ScopeRT scope, IEntityContext context) {
        CopyNodeParam param = nodeDTO.getParam();
        var parsingContext = FlowParsingContext.create(scope, prev, context);
        var source = ValueFactory.create(param.source(), parsingContext);
        ParentRef parentRef;
        if (param.parentRef() != null) {
            parentRef = new ParentRef(
                    ValueFactory.create(param.parentRef().parent(), parsingContext),
                    NncUtils.get(param.parentRef().fieldRef(), ref -> FieldRef.create(ref, context))
            );
        } else
            parentRef = null;
        CopyNode node;
        if (nodeDTO.id() != null) {
            node = (CopyNode) context.getNode(Id.parse(nodeDTO.id()));
            node.setSource(source);
            node.setParentRef(parentRef);
        } else
            node = new CopyNode(nodeDTO.tmpId(), nodeDTO.name(), nodeDTO.code(), source, parentRef, prev, scope);
        return node;
    }

    private Value source;
    @Nullable
    private ParentRef parentRef;

    protected CopyNode(Long tmpId, String name, @Nullable String code, Value source, @Nullable ParentRef parentRef, NodeRT previous, ScopeRT scope) {
        super(tmpId, name, code, source.getType(), previous, scope);
        this.source = source;
        this.parentRef = parentRef;
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitCopyNode(this);
    }

    @Override
    protected CopyNodeParam getParam(SerializeContext serializeContext) {
        return new CopyNodeParam(
                source.toDTO(),
                parentRef != null ? parentRef.toDTO() : null
        );
    }

    public void setSource(Value source) {
        this.source = source;
    }

    public void setParentRef(@Nullable ParentRef parentRef) {
        this.parentRef = parentRef;
    }

    @Override
    public NodeExecResult execute(MetaFrame frame) {
        var sourceInst = source.evaluate(frame);
        var copy = sourceInst.resolveDurable().copy();
        if (parentRef != null) {
            var instParentRef = parentRef.evaluate(frame);
            copy.setParent(instParentRef.parent().resolve(), instParentRef.field());
        }
        return next(copy.getReference());
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("copy " + source.getText());
        if (parentRef != null)
            writer.write(" " + parentRef.getText());
    }
}
