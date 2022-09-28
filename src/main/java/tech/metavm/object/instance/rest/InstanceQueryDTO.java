package tech.metavm.object.instance.rest;

public record InstanceQueryDTO (
        long typeId,
        String searchText,
        int page,
        int pageSize
) {

    public long start() {
        return (long) (page - 1) * pageSize;
    }

    public long limit() {
        return pageSize;
    }

}
