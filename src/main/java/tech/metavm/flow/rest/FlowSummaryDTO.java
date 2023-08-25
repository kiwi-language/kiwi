package tech.metavm.flow.rest;

public record FlowSummaryDTO(
    Long id,
    String name,
    Long typeId,
    Long inputTypeId,
    Long outputTypeId,
    boolean inputRequired
) {

}
