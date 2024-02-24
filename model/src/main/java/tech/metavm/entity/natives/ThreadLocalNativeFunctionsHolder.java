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

    @Override
    public Function getFunctionToInstance() {
        return TL.get().getFunctionToInstance();
    }

    @Override
    public void setFunctionToInstance(Function function) {
        TL.get().setFunctionToInstance(function);
    }

    @Override
    public void setSendEmail(Function function) {
        TL.get().setSendEmail(function);
    }

    @Override
    public Function getSendEmail() {
        return TL.get().getSendEmail();
    }

    @Override
    public void setEmailSender(EmailSender emailSender) {
        TL.get().setEmailSender(emailSender);
    }

    @Override
    public Function getGetSessionEntry() {
        return TL.get().getGetSessionEntry();
    }

    @Override
    public void setGetSessionEntry(Function function) {
        TL.get().setGetSessionEntry(function);
    }

    @Override
    public void setSetSessionEntry(Function function) {
        TL.get().setSetSessionEntry(function);
    }

    @Override
    public Function getTypeCast() {
        return TL.get().getTypeCast();
    }

    @Override
    public void setTypeCast(Function function) {
        TL.get().setTypeCast(function);
    }

    @Override
    public Function getPrint() {
        return TL.get().getPrint();
    }

    @Override
    public void setPrint(Function function) {
        TL.get().setPrint(function);
    }

    @Override
    public Function getRemoveSessionEntry() {
        return TL.get().getRemoveSessionEntry();
    }

    @Override
    public void setRemoveSessionEntry(Function function) {
        TL.get().setRemoveSessionEntry(function);
    }

    @Override
    public Function getSetSessionEntry() {
        return TL.get().getSetSessionEntry();
    }
}
