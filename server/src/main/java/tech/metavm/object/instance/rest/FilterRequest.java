package tech.metavm.object.instance.rest;

public record FilterRequest(
        long fieldId,
        InstanceDTO instance,
        int page,
        int pageSize
) {
}
