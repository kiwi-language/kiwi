package org.metavm.flow;

import org.metavm.api.EntityType;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.IEntityContext;
import org.metavm.entity.SerializeContext;
import org.metavm.flow.rest.Bytecodes;
import org.metavm.flow.rest.MethodCallNodeParam;
import org.metavm.flow.rest.NodeDTO;
import org.metavm.object.type.TypeParser;
import org.metavm.util.NncUtils;

import java.util.Objects;

@EntityType
public class MethodCallNode extends CallNode {

    public static MethodCallNode save(NodeDTO nodeDTO, NodeRT prev, Code code, NodeSavingStage stage, IEntityContext context) {
        var node = (MethodCallNode) context.getNode(nodeDTO.id());
        if (node == null) {
            MethodCallNodeParam param = nodeDTO.getParam();
            var methodRef = MethodRef.createMethodRef(Objects.requireNonNull(param.getFlowRef()), context);
            node = new MethodCallNode(nodeDTO.tmpId(), nodeDTO.name(), prev, code, methodRef);
            node.setCapturedVariableTypes(NncUtils.map(param.getCapturedVariableTypes(), t -> TypeParser.parseType(t, context)));
            node.setCapturedVariableIndexes(param.getCapturedVariableIndexes());
        }
        return node;
    }

    public MethodCallNode(Long tmpId,
                          String name,
                          NodeRT prev,
                          Code code,
                          MethodRef methodRef) {
        super(tmpId, name, prev, code, methodRef);
    }

    @Override
    protected MethodCallNodeParam getParam(SerializeContext serializeContext) {
        var method = getMethod();
        return new MethodCallNodeParam(
                getFlowRef().toDTO(serializeContext),
                null,
                null,
                method.getDeclaringType().getType().toExpression(serializeContext),
                NncUtils.map(capturedVariableTypes, t -> t.toExpression(serializeContext)),
                capturedVariableIndexes.toList()
        );
    }

    @Override
    public MethodRef getFlowRef() {
        return (MethodRef) super.getFlowRef();
    }

    @Override
    public void writeContent(CodeWriter writer) {
        var method = getFlowRef().resolve();
        writer.write("invoke " + method.getQualifiedSignature());
    }

    @Override
    public void writeCode(CodeOutput output) {
        output.write(Bytecodes.METHOD_CALL);
        writeCallCode(output);
    }

    private Method getMethod() {
        return (Method) super.getFlowRef().resolve();
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitSubFlowNode(this);
    }
}
