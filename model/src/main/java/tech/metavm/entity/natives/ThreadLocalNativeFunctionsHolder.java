package tech.metavm.entity.natives;

import org.jetbrains.annotations.NotNull;
import tech.metavm.flow.FlowExecResult;
import tech.metavm.flow.Function;
import tech.metavm.object.instance.core.Instance;

import java.util.List;

public class ThreadLocalNativeFunctionsHolder implements NativeFunctionsHolder {

    private final ThreadLocal<GlobalNativeFunctionsHolder> TL = ThreadLocal.withInitial(GlobalNativeFunctionsHolder::new);

    @Override
    public Function getGetSourceFunc() {
        return TL.get().getGetSourceFunc();
    }

    @Override
    public Function getSetSourceFunc() {
        return TL.get().getSetSourceFunc();
    }

    @Override
    public Function getIsSourcePresentFunc() {
        return TL.get().getIsSourcePresentFunc();
    }

    @Override
    public void setGetSourceFunc(@NotNull Function function) {
        TL.get().setGetSourceFunc(function);
    }

    @Override
    public void setIsSourcePresent(@NotNull Function function) {
        TL.get().setIsSourcePresent(function);
    }

    @Override
    public void setSetSourceFunc(@NotNull Function function) {
        TL.get().setSetSourceFunc(function);
    }

    @Override
    public FlowExecResult invoke(@NotNull Function flow, @NotNull List<Instance> arguments) {
        return TL.get().invoke(flow, arguments);
    }
}
