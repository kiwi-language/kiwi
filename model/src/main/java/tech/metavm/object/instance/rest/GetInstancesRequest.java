package tech.metavm.object.instance.rest;

import tech.metavm.common.Request;

import java.util.List;

public final class GetInstancesRequest extends Request {
    private final List<Long> ids;
    private final int depth;

    public GetInstancesRequest(List<Long> ids, int depth) {
        this.ids = ids;
        this.depth = depth;
    }

    public List<Long> getIds() {
        return ids;
    }

    public int getDepth() {
        return depth;
    }
}
