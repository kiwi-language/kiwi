package tech.metavm.entity.natives;

import org.jetbrains.annotations.NotNull;
import tech.metavm.flow.FlowExecResult;
import tech.metavm.flow.Function;
import tech.metavm.object.instance.core.Instance;

import java.util.List;

public class NativeFunctions {

    private static NativeFunctionsHolder holder = new GlobalNativeFunctionsHolder();

    public static void setHolder(NativeFunctionsHolder holder) {
        NativeFunctions.holder = holder;
    }

    public static FlowExecResult invoke(@NotNull Function flow, @NotNull List<Instance> arguments) {
        return holder.invoke(flow, arguments);
    }

    public static Function getSource() {
        return holder.getGetSourceFunc();
    }

    public static Function setSource() {
        return holder.getSetSourceFunc();
    }

    public static Function isSourcePresent() {
        return holder.getIsSourcePresentFunc();
    }

    public static void setGetSourceFunc(@NotNull Function function) {
        holder.setGetSourceFunc(function);
    }

    public static void setIsSourcePresent(@NotNull Function function) {
        holder.setIsSourcePresent(function);
    }

    public static void setSetSourceFunc(@NotNull Function function) {
        holder.setSetSourceFunc(function);
    }

    public static Function getFunctionToInstance() {
        return holder.getFunctionToInstance();
    }

    public static void setFunctionToInstance(@NotNull Function function) {
        holder.setFunctionToInstance(function);
    }

    public static Function getSendEmail() {
        return holder.getSendEmail();
    }

    public static void setSendEmail(@NotNull Function function) {
        holder.setSendEmail(function);
    }

    public static void setEmailSender(EmailSender emailSender) {
        holder.setEmailSender(emailSender);
    }

    public static Function getGetSessionEntry() {
        return holder.getGetSessionEntry();
    }

    public static void setGetSessionEntry(@NotNull Function function) {
        holder.setGetSessionEntry(function);
    }

    public static void setSetSessionEntry(@NotNull Function function) {
        holder.setSetSessionEntry(function);
    }

    public static Function getSetSessionEntry() {
        return holder.getSetSessionEntry();
    }

    public static Function getRemoveSessionEntry() {
        return holder.getRemoveSessionEntry();
    }

    public static void setRemoveSessionEntry(@NotNull Function function) {
        holder.setRemoveSessionEntry(function);
    }

    public static Function getTypeCast() {
        return holder.getTypeCast();
    }

    public static void setTypeCast(@NotNull Function function) {
        holder.setTypeCast(function);
    }

    public static Function getPrint() {
        return holder.getPrint();
    }

    public static void setPrint(@NotNull Function function) {
        holder.setPrint(function);
    }

}
