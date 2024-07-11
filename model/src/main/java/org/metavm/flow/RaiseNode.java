package org.metavm.flow;

import org.metavm.api.EntityType;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.IEntityContext;
import org.metavm.entity.SerializeContext;
import org.metavm.entity.StdKlass;
import org.metavm.entity.natives.ExceptionNative;
import org.metavm.entity.natives.NativeMethods;
import org.metavm.expression.FlowParsingContext;
import org.metavm.flow.rest.NodeDTO;
import org.metavm.flow.rest.RaiseNodeParam;
import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.instance.core.Id;
import org.metavm.util.InternalException;
import org.metavm.util.NncUtils;

import javax.annotation.Nullable;

@EntityType
public class RaiseNode extends NodeRT {

    public static RaiseNode save(NodeDTO nodeDTO, NodeRT prev, ScopeRT scope, IEntityContext entityContext) {
        var parsingContext = FlowParsingContext.create(scope, prev, entityContext);
        RaiseNodeParam param = nodeDTO.getParam();
        var paramKind = RaiseParameterKind.getByCode(param.parameterKind());
        var exception = param.exception() != null ? ValueFactory.create(param.exception(), parsingContext) : null;
        var message = param.message() != null ? ValueFactory.create(param.message(), parsingContext) : null;
        RaiseNode node = (RaiseNode) entityContext.getNode(Id.parse(nodeDTO.id()));
        if (node != null)
            node.update(paramKind, exception, message);
        else
            node = new RaiseNode(nodeDTO.tmpId(), nodeDTO.name(), nodeDTO.code(), prev, scope, paramKind,
                    exception, message);
        return node;
    }

    private @Nullable Value message;

    private @Nullable Value exception;

    private RaiseParameterKind parameterKind;

    public RaiseNode(Long tmpId, String name, @Nullable String code, NodeRT prev, ScopeRT scope,
                     RaiseParameterKind parameterKind, @Nullable Value exception, @Nullable Value message) {
        super(tmpId, name, code, null, prev, scope);
        this.parameterKind = parameterKind;
        if(parameterKind == RaiseParameterKind.THROWABLE) {
            this.exception = exception;
            this.message = null;
        }
        else {
            this.exception = null;
            this.message = message;
        }
    }

    public void update(RaiseParameterKind parameterKind, @Nullable Value exception, @Nullable Value message) {
        this.parameterKind = parameterKind;
        if(parameterKind == RaiseParameterKind.THROWABLE) {
            this.exception = exception;
            this.message = null;
        }
        else {
            this.exception = null;
            this.message = message;
        }
    }

    private Value checkException(Value exception) {
        return exception;
    }

    private Value checkMessage(Value message) {
        return message;
    }

    public RaiseParameterKind getParameterKind() {
        return parameterKind;
    }

    @Override
    protected RaiseNodeParam getParam(SerializeContext serializeContext) {
        return new RaiseNodeParam(
                parameterKind.getCode(),
                NncUtils.get(message, Value::toDTO),
                NncUtils.get(exception, Value::toDTO)
        );
    }

    @Override
    public NodeExecResult execute(MetaFrame frame) {
        if (exception != null) {
            return NodeExecResult.exception(this.exception.evaluate(frame).resolveObject());
        } else {
            NncUtils.requireNonNull(message);
            var exceptionInst = ClassInstance.allocate(StdKlass.exception.get().getType());
            ExceptionNative nativeObj = (ExceptionNative) NativeMethods.getNativeObject(exceptionInst);
            nativeObj.Exception(message.evaluate(frame), frame);
            return frame.catchException(this, exceptionInst);
        }
    }

    @Override
    public void writeContent(CodeWriter writer) {
        if(exception != null)
            writer.write("raise " + exception.getText());
        else if(message != null)
            writer.write("raise Error(" + message.getText() + ")");
        else
            throw new InternalException("Invalid raise node");
    }

    public @Nullable Value getException() {
        return exception;
    }

    @Nullable
    public Value getMessage() {
        return message;
    }

    @Override
    public boolean isExit() {
        return true;
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitRaiseNode(this);
    }
}
