package org.manul.object.instance.rest;

public record TreeDTO(
        long id, long version, long nextNodeId, byte[] bytes
) {
}
