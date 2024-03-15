package tech.metavm.flow;

import tech.metavm.entity.*;
import tech.metavm.expression.FlowParsingContext;
import tech.metavm.flow.rest.CopyNodeParam;
import tech.metavm.flow.rest.NodeDTO;
import tech.metavm.object.instance.core.Id;
import tech.metavm.object.instance.core.InstanceCopier;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;

@EntityType("复制节点")
public class CopyNode extends NodeRT {

    public static CopyNode save(NodeDTO nodeDTO, NodeRT prev, ScopeRT scope, IEntityContext context) {
        CopyNodeParam param = nodeDTO.getParam();
        var parsingContext = FlowParsingContext.create(scope, prev, context);
        var source = ValueFactory.create(param.source(), parsingContext);
        ParentRef parentRef;
        if (param.parentRef() != null) {
            parentRef = new ParentRef(
                    ValueFactory.create(param.parentRef().parent(), parsingContext),
                    NncUtils.get(param.parentRef().fieldId(), id -> context.getField(Id.parse(id)))
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

    @ChildEntity("复制源")
    private Value source;
    @ChildEntity("父引用")
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
        var copier = new InstanceCopier(sourceInst);
        if (parentRef != null) {
            var instParentRef = parentRef.evaluate(frame);
            copier.setCurrentInstance(instParentRef.parent());
            copier.setCurrentField(instParentRef.field());
        }
        return next(sourceInst.accept(copier));
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("copy " + source.getText());
        if (parentRef != null)
            writer.write(" " + parentRef.getText());
    }
}
