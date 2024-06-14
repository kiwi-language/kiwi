package org.metavm.object.instance.rest;

import org.metavm.common.Request;

import java.util.List;

public final class GetInstancesRequest extends Request {
    private final List<String> ids;
    private final int depth;

    public GetInstancesRequest(List<String> ids, int depth) {
        this.ids = ids;
        this.depth = depth;
    }

    public List<String> getIds() {
        return ids;
    }

    public int getDepth() {
        return depth;
    }
}
