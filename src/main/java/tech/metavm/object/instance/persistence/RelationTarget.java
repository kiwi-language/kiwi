package tech.metavm.object.instance.persistence;

public record RelationTarget(
        long destInstanceId,
        Long fieldId
) {

}
