package org.metavm.object.instance.rest;

import java.util.List;

public record GraphQueryDTO (
        long typeId,
        List<String> paths,
        String condition,
        int page,
        int pageSize
) {

}
