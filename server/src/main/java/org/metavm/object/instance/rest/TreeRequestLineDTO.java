package org.metavm.object.instance.rest;

import java.util.List;

public record TreeRequestLineDTO(
        long id,
        List<String> paths
) {
}
