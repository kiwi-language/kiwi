package tech.metavm.flow;

import tech.metavm.entity.ChildEntity;
import tech.metavm.entity.EntityField;
import tech.metavm.entity.EntityType;
import tech.metavm.entity.IEntityContext;
import tech.metavm.entity.natives.ExceptionNative;
import tech.metavm.entity.natives.NativeInvoker;
import tech.metavm.entity.ElementVisitor;
import tech.metavm.expression.FlowParsingContext;
import tech.metavm.flow.rest.ExceptionParamDTO;
import tech.metavm.flow.rest.NodeDTO;
import tech.metavm.object.instance.core.ClassInstance;
import tech.metavm.object.meta.StandardTypes;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;

@EntityType("异常节点")
public class RaiseNode extends NodeRT<ExceptionParamDTO> {

    public static RaiseNode create(NodeDTO nodeDTO, NodeRT<?> prev, ScopeRT scope, IEntityContext entityContext) {
        ExceptionParamDTO param = nodeDTO.getParam();
        var parsingContext = FlowParsingContext.create(scope, prev, entityContext.getInstanceContext());
        var exception = param.exception() != null ? ValueFactory.create(param.exception(), parsingContext) : null;
        var message = param.message() != null ? ValueFactory.create(param.message(), parsingContext) : null;
        return new RaiseNode(nodeDTO.tmpId(), nodeDTO.name(), RaiseParameterKind.getByCode(param.parameterKind()),
                exception, message, prev, scope);
    }

    @ChildEntity("错误信息")
    @Nullable
    private Value message;

    @ChildEntity("异常")
    @Nullable
    private Value exception;

    @EntityField("参数类型")
    private RaiseParameterKind parameterKind;

    public RaiseNode(Long tmpId, String name, RaiseParameterKind parameterKind,
                     @Nullable Value exception, @Nullable Value message,  NodeRT<?> prev, ScopeRT scope) {
        super(tmpId, name, null, prev, scope);
        this.parameterKind = parameterKind;
        this.exception = checkException(exception);
        this.message = checkMessage(message);
    }

    @Override
    protected void setParam(ExceptionParamDTO param, IEntityContext entityContext) {
        if(param.parameterKind() != null) {
            parameterKind = RaiseParameterKind.getByCode(param.parameterKind());
            switch (parameterKind) {
                case MESSAGE -> exception = null;
                case THROWABLE -> message = null;
            }
        }
        if (param.exception() != null) {
            exception = checkException(ValueFactory.create(param.exception(), getParsingContext(entityContext)));
        }
        if(param.message() != null) {
            message = checkMessage(ValueFactory.create(param.message(), getParsingContext(entityContext)));
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
    protected ExceptionParamDTO getParam(boolean persisting) {
        return new ExceptionParamDTO(
                parameterKind.getCode(),
                NncUtils.get(message, msg -> msg.toDTO(persisting)),
                NncUtils.get(exception, e -> e.toDTO(persisting))
        );
    }

    @Override
    public void execute(MetaFrame frame) {
        if(exception != null) {
            frame.exception((ClassInstance) this.exception.evaluate(frame));
        }
        else {
            NncUtils.requireNonNull(message);
            var exceptionInst = new ClassInstance(StandardTypes.getExceptionType());
            ExceptionNative nativeObj = (ExceptionNative) NativeInvoker.getNativeObject(exceptionInst);
            nativeObj.Exception(message.evaluate(frame));
            frame.exception(exceptionInst);
        }
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
