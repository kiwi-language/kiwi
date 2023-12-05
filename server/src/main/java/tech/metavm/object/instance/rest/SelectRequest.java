package tech.metavm.object.instance.rest;

import java.util.List;

public record SelectRequest(
        long typeId,
        List<String> selects,
        String condition,
        int page,
        int pageSize
) {

}
