package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.IEntityContext;
import org.metavm.entity.SerializeContext;
import org.metavm.expression.FlowParsingContext;
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
            var parsingContext = FlowParsingContext.create(scope, prev, context);
            var first = ValueFactory.create(param.instance(), parsingContext);
            var propertyRef = PropertyRef.create(param.propertyRef(), context);
            node = new GetPropertyNode(nodeDTO.tmpId(), nodeDTO.name(), nodeDTO.code(),
                    prev, scope, first, propertyRef);
        }
        return node;
    }

    private final Value instance;
    private final PropertyRef propertyRef;

    public GetPropertyNode(Long tmpId,
                           @NotNull String name,
                           @Nullable String code,
                           @Nullable NodeRT previous,
                           @NotNull ScopeRT scope,
                           Value instance,
                           PropertyRef propertyRef) {
        super(tmpId, name, code, null, previous, scope);
        this.instance = instance;
        this.propertyRef = propertyRef;
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitGetFieldNode(this);
    }

    @Override
    protected Object getParam(SerializeContext serializeContext) {
        return new GetPropertyNodeParam(instance.toDTO(), propertyRef.toDTO(serializeContext));
    }

    @NotNull
    public Type getType() {
        return propertyRef.resolve().getType();
    }

    @Override
    public NodeExecResult execute(MetaFrame frame) {
        var i = instance.evaluate(frame).resolveObject();
        var p = propertyRef.resolve();
        return next(i.getProperty(p));
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write(instance.getText() + "." + propertyRef.resolve().getName());
    }
}
