package tech.metavm.entity.natives;

import org.jetbrains.annotations.NotNull;
import tech.metavm.flow.FlowExecResult;
import tech.metavm.flow.Function;
import tech.metavm.object.instance.core.Instance;

import java.util.List;

public interface NativeFunctionsHolder {

    Function getGetSourceFunc();

    Function getSetSourceFunc();

    Function getIsSourcePresentFunc();

    void setGetSourceFunc(@NotNull Function function);

    void setIsSourcePresent(@NotNull Function function);

    void setSetSourceFunc(@NotNull Function function);

    FlowExecResult invoke(@NotNull Function flow, @NotNull List<Instance> arguments);

}
