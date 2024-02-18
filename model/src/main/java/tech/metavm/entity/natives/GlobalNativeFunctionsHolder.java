package tech.metavm.entity.natives;

import org.jetbrains.annotations.NotNull;
import tech.metavm.flow.FlowExecResult;
import tech.metavm.flow.Function;
import tech.metavm.object.instance.core.*;
import tech.metavm.object.type.ClassType;
import tech.metavm.object.type.Types;
import tech.metavm.util.Instances;
import tech.metavm.util.InternalException;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;

public class GlobalNativeFunctionsHolder implements NativeFunctionsHolder {

    private final Map<Function, BiFunction<Function, List<Instance>, Instance>> functions = new IdentityHashMap<>();

    private Function isSourcePresentFunc;

    private Function getSourceFunc;

    private Function setSourceFunc;

    private Function functionToInstance;

    private static Instance getSource(@NotNull Instance instance) {
        if(instance instanceof DurableInstance durableInstance)
            return durableInstance.getSource();
        else
            throw new InternalException("Can not get source of a non-durable instance: " + instance);
    }

    public static BooleanInstance isSourcePresent(@NotNull Instance instance) {
        if(instance instanceof DurableInstance durableInstance)
            return Instances.booleanInstance(durableInstance.tryGetSource() != null);
        else
            throw new InternalException("Can not get source of a non-durable instance: " + instance);
    }

    private static Instance setSource(@NotNull Instance instance, @NotNull Instance source) {
        if(instance instanceof DurableInstance durableInstance) {
            durableInstance.setSourceRef(new SourceRef((DurableInstance) source, null));
            return Instances.nullInstance();
        }
        else
            throw new InternalException("Can not set source for a non-durable instance: " + instance);
    }

    private static Instance functionToInstance(Function function, @NotNull FunctionInstance functionInstance) {
        var samInterface = (ClassType) function.getTypeArguments().get(0);
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
        functions.put(function, (func, args) -> getSource(args.get(0)));
    }

    @Override
    public void setIsSourcePresent(@NotNull Function function) {
        isSourcePresentFunc = function;
        functions.put(function, (func, args) -> isSourcePresent(args.get(0)));
    }

    @Override
    public void setSetSourceFunc(@NotNull Function function) {
        setSourceFunc = function;
        functions.put(function, (func, args) -> setSource(args.get(0), args.get(1)));
    }

    @Override
    public FlowExecResult invoke(@NotNull Function flow, @NotNull List<Instance> arguments) {
        var func = Objects.requireNonNull(functions.get(flow.getEffectiveHorizontalTemplate()));
        return new FlowExecResult(func.apply(flow, arguments), null);
    }

    @Override
    public Function getFunctionToInstance() {
        return functionToInstance;
    }

    @Override
    public void setFunctionToInstance(Function function) {
        this.functionToInstance = function;
        functions.put(function, (func, args) -> functionToInstance(func, (FunctionInstance) args.get(0)));
    }
}
