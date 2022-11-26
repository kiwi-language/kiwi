package tech.metavm.infra.persistence;

public record RangeCreatingRequest (
        long typeId,
        long size
) {

}
