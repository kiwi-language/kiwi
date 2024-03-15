package tech.metavm.object.instance.rest;

import java.util.List;

public record SelectRequest(
        String typeId,
        List<String> selects,
        String condition,
        int page,
        int pageSize
) {

}
