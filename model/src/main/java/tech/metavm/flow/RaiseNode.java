package tech.metavm.flow;

import tech.metavm.entity.*;
import tech.metavm.entity.natives.ExceptionNative;
import tech.metavm.entity.natives.CallContext;
import tech.metavm.entity.natives.NativeMethods;
import tech.metavm.expression.FlowParsingContext;
import tech.metavm.flow.rest.RaiseNodeParam;
import tech.metavm.flow.rest.NodeDTO;
import tech.metavm.object.instance.core.ClassInstance;
import tech.metavm.object.instance.core.Id;
import tech.metavm.util.InternalException;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.Objects;

@EntityType("抛出节点")
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

    @ChildEntity("错误信息")
    @Nullable
    private Value message;

    @ChildEntity("异常")
    @Nullable
    private Value exception;

    @EntityField("参数类型")
    private RaiseParameterKind parameterKind;

    public RaiseNode(Long tmpId, String name, @Nullable String code, NodeRT prev, ScopeRT scope,
                     RaiseParameterKind parameterKind, @Nullable Value exception, @Nullable Value message) {
        super(tmpId, name, code, null, prev, scope);
        this.parameterKind = parameterKind;
        if(parameterKind == RaiseParameterKind.THROWABLE) {
            this.exception = addChild(Objects.requireNonNull(exception), "exception");
            this.message = null;
        }
        else {
            this.exception = null;
            this.message = addChild(Objects.requireNonNull(message), "message");
        }
    }

    public void update(RaiseParameterKind parameterKind, @Nullable Value exception, @Nullable Value message) {
        this.parameterKind = parameterKind;
        if(parameterKind == RaiseParameterKind.THROWABLE) {
            this.exception = addChild(Objects.requireNonNull(exception), "exception");
            this.message = null;
        }
        else {
            this.exception = null;
            this.message = addChild(Objects.requireNonNull(message), "message");
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
            return NodeExecResult.exception((ClassInstance) this.exception.evaluate(frame));
        } else {
            NncUtils.requireNonNull(message);
            var exceptionInst = ClassInstance.allocate(StandardTypes.getExceptionType());
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
