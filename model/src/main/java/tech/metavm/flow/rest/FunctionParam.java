package tech.metavm.flow.rest;

public record FunctionParam() implements FlowParam {
    @Override
    public int getKind() {
        return 1;
    }
}
