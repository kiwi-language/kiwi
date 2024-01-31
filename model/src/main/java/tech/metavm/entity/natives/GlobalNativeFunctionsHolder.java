package tech.metavm.entity.natives;

import org.jetbrains.annotations.NotNull;
import tech.metavm.flow.FlowExecResult;
import tech.metavm.flow.Function;
import tech.metavm.object.instance.core.BooleanInstance;
import tech.metavm.object.instance.core.DurableInstance;
import tech.metavm.object.instance.core.Instance;
import tech.metavm.object.instance.core.SourceRef;
import tech.metavm.util.Instances;
import tech.metavm.util.InternalException;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class GlobalNativeFunctionsHolder implements NativeFunctionsHolder {

    private final Map<Function, java.util.function.Function<List<Instance>, Instance>> functions = new IdentityHashMap<>();

    private Function isSourcePresentFunc;

    private Function getSourceFunc;

    private Function setSourceFunc;


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
        functions.put(function, args -> getSource(args.get(0)));
    }

    @Override
    public void setIsSourcePresent(@NotNull Function function) {
        isSourcePresentFunc = function;
        functions.put(function, args -> isSourcePresent(args.get(0)));
    }

    @Override
    public void setSetSourceFunc(@NotNull Function function) {
        setSourceFunc = function;
        functions.put(function, args -> setSource(args.get(0), args.get(1)));
    }

    @Override
    public FlowExecResult invoke(@NotNull Function flow, @NotNull List<Instance> arguments) {
        var func = Objects.requireNonNull(functions.get(flow));
        return new FlowExecResult(func.apply(arguments), null);
    }
}
