package tech.metavm.object.instance.rest;

public record TreeDTO(
        long id, long version, int nextNodeId, byte[] bytes
) {
}
