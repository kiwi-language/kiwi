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

public class NativeFunctions {

    private static final Map<Function, java.util.function.Function<List<Instance>, Instance>> FUNCTIONS = new IdentityHashMap<>();

    private static Function IS_SOURCE_PRESENT;

    private static Function GET_SOURCE;

    private static Function SET_SOURCE;

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

    public static FlowExecResult invoke(@NotNull Function flow, @NotNull List<Instance> arguments) {
        var func = Objects.requireNonNull(FUNCTIONS.get(flow));
        return new FlowExecResult(func.apply(arguments), null);
    }

    public static Function getSource() {
        return GET_SOURCE;
    }

    public static Function setSource() {
        return SET_SOURCE;
    }

    public static Function isSourcePresent() {
        return IS_SOURCE_PRESENT;
    }

    public static void setGetSourceFunc(@NotNull Function function) {
        GET_SOURCE = function;
        FUNCTIONS.put(function, args -> getSource(args.get(0)));
    }

    public static void setIsSourcePresent(@NotNull Function function) {
        IS_SOURCE_PRESENT = function;
        FUNCTIONS.put(function, args -> isSourcePresent(args.get(0)));
    }

    public static void setSetSourceFunc(@NotNull Function function) {
        SET_SOURCE = function;
        FUNCTIONS.put(function, args -> setSource(args.get(0), args.get(1)));
    }

}
