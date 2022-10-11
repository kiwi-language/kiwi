package tech.metavm.flow.rest;

public record FlowSummaryDTO(
    long id,
    String name,
    long typeId,
    Long inputTypeId
) {

}
