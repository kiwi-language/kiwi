package tech.metavm.flow.rest;

public record CheckNodeParam(
        ValueDTO condition,
        String exitId
) {
}
