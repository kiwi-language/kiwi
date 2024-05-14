package tech.metavm.entity.natives;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.metavm.common.ErrorCode;
import tech.metavm.flow.FlowExecResult;
import tech.metavm.flow.Function;
import tech.metavm.object.instance.core.*;
import tech.metavm.object.type.ClassType;
import tech.metavm.object.type.Types;
import tech.metavm.user.Session;
import tech.metavm.util.*;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class GlobalNativeFunctionsHolder implements NativeFunctionsHolder {

    public static final Logger logger = LoggerFactory.getLogger(GlobalNativeFunctionsHolder.class);

    private final Map<Function, TriFunction<Function, List<Instance>, CallContext, Instance>> functions = new IdentityHashMap<>();

    private EmailSender emailSender;

    private Function isSourcePresentFunc;

    private Function getSourceFunc;

    private Function setSourceFunc;

    private Function functionToInstance;

    private Function sendEmail;

    private Function getSessionEntry;

    private Function setSessionEntry;

    private Function removeSessionEntry;

    private Function typeCast;

    private Function print;

    private Function delete;

    private static Instance getSource(@NotNull Instance instance) {
        if (instance instanceof DurableInstance durableInstance)
            return durableInstance.getSource();
        else
            throw new InternalException("Can not get source of a non-durable instance: " + instance);
    }

    public static BooleanInstance isSourcePresent(@NotNull Instance instance) {
        if (instance instanceof DurableInstance durableInstance)
            return Instances.booleanInstance(durableInstance.tryGetSource() != null);
        else
            throw new InternalException("Can not get source of a non-durable instance: " + instance);
    }

    private static Instance setSource(@NotNull Instance instance, @NotNull Instance source) {
        if (instance instanceof DurableInstance durableInstance) {
            durableInstance.setSourceRef(new SourceRef((DurableInstance) source, null));
            return Instances.nullInstance();
        } else
            throw new InternalException("Can not set source for a non-durable instance: " + instance);
    }

    private static Instance functionToInstance(Function function, @NotNull FunctionInstance functionInstance) {
        var samInterface = ((ClassType) function.getTypeArguments().get(0)).resolve();
        var type = Types.createSAMInterfaceImpl(samInterface, functionInstance);
        return new ClassInstance(null, Map.of(), type);
    }

    @Override
    public Function getGetSourceFunc() {
        return getSourceFunc;
    }

    @Override
    public Function getSetSourceFunc() {
        return setSourceFunc;
    }

    @Override
    public Function getIsSourcePresentFunc() {
        return isSourcePresentFunc;
    }

    @Override
    public void setGetSourceFunc(@NotNull Function function) {
        getSourceFunc = function;
        functions.put(function, (func, args, callContext) -> getSource(args.get(0)));
    }

    @Override
    public void setIsSourcePresent(@NotNull Function function) {
        isSourcePresentFunc = function;
        functions.put(function, (func, args, callContext) -> isSourcePresent(args.get(0)));
    }

    @Override
    public void setSetSourceFunc(@NotNull Function function) {
        setSourceFunc = function;
        functions.put(function, (func, args, callContext) -> setSource(args.get(0), args.get(1)));
    }

    @Override
    public FlowExecResult invoke(@NotNull Function flow, @NotNull List<Instance> arguments, CallContext callContext) {
        var func = Objects.requireNonNull(functions.get(flow.getEffectiveHorizontalTemplate()));
        return new FlowExecResult(func.apply(flow, arguments, callContext), null);
    }

    @Override
    public Function getFunctionToInstance() {
        return functionToInstance;
    }

    @Override
    public void setFunctionToInstance(Function function) {
        this.functionToInstance = function;
        functions.put(function, (func, args, callContext) -> {
            if(args.get(0) instanceof FunctionInstance) {
                return functionToInstance(func, (FunctionInstance) args.get(0));
            }
            else {
                throw new InternalException("Invalid function instance: " + Instances.getInstancePath(args.get(0)));
            }
        });
    }

    @Override
    public void setSendEmail(Function function) {
        this.sendEmail = function;
        functions.put(function, (func, args, callContext) -> {
            emailSender.send(
                    ((StringInstance) args.get(0)).getValue(),
                    ((StringInstance) args.get(1)).getValue(),
                    ((StringInstance) args.get(2)).getValue()
            );
            return Instances.nullInstance();
        });
    }

    @Override
    public Function getSendEmail() {
        return sendEmail;
    }

    @Override
    public void setEmailSender(EmailSender emailSender) {
        this.emailSender = emailSender;
    }

    @Override
    public Function getGetSessionEntry() {
        return getSessionEntry;
    }

    @Override
    public void setGetSessionEntry(Function function) {
        this.getSessionEntry = function;
        functions.put(function, (func, args, callContext) -> {
            var key = ((StringInstance) args.get(0)).getValue();
            var entityContext = ContextUtil.getEntityContext();
            var session = entityContext.selectFirstByKey(Session.IDX_TOKEN, ContextUtil.getToken());
            if(session == null || !session.isActive())
                throw new BusinessException(ErrorCode.LOGIN_REQUIRED);
            var value = session.getEntry(key);
            return NncUtils.orElse(value, Instances.nullInstance());
        });
    }

    @Override
    public Function getSetSessionEntry() {
        return setSessionEntry;
    }

    @Override
    public void setSetSessionEntry(Function function) {
        this.setSessionEntry = function;
        functions.put(function, (func, args, callContext) -> {
            var key = ((StringInstance) args.get(0)).getValue();
            var value = args.get(1);
            var entityContext = ContextUtil.getEntityContext();
            var session = entityContext.selectFirstByKey(Session.IDX_TOKEN, ContextUtil.getToken());
            if(session == null || !session.isActive())
                throw new BusinessException(ErrorCode.LOGIN_REQUIRED);
            session.setEntry(key, value);
            return Instances.nullInstance();
        });
    }

    @Override
    public Function getRemoveSessionEntry() {
        return removeSessionEntry;
    }

    @Override
    public void setRemoveSessionEntry(Function function) {
        this.removeSessionEntry = function;
        functions.put(function, (func, args, callContext) -> {
            var key = ((StringInstance) args.get(0)).getValue();
            var entityContext = ContextUtil.getEntityContext();
            var session = entityContext.selectFirstByKey(Session.IDX_TOKEN, ContextUtil.getToken());
            if(session == null || !session.isActive())
                throw new BusinessException(ErrorCode.LOGIN_REQUIRED);
            return Instances.booleanInstance(session.removeEntry(key));
        });
    }

    @Override
    public Function getDelete() {
        return delete;
    }

    @Override
    public void setDelete(Function function) {
        this.delete = function;
        functions.put(function, (func, args, callContext) -> {
            var entityContext = ContextUtil.getEntityContext();
            var instance = args.get(0);
            if(instance instanceof DurableInstance durableInstance) {
                entityContext.getInstanceContext().remove(durableInstance);
                return Instances.nullInstance();
            } else
                throw new BusinessException(ErrorCode.DELETE_NON_DURABLE_INSTANCE, instance);
        });
    }

    @Override
    public Function getTypeCast() {
        return typeCast;
    }

    @Override
    public void setTypeCast(Function function) {
        this.typeCast = function;
        functions.put(function, (func, args, callContext) -> {
            var type = func.getTypeArguments().get(0);
            var value = args.get(0);
            if(type.isInstance(value))
                return value;
            else
                throw new BusinessException(ErrorCode.TYPE_CAST_ERROR, value.getType(), type);
        });
    }

    @Override
    public Function getPrint() {
        return print;
    }

    @Override
    public void setPrint(Function function) {
        this.print = function;
        functions.put(function, (func, args, callContext) -> {
            System.out.println(args.get(0).getTitle());
            return null;
        });
    }

}
