package tech.metavm.object.instance;

import tech.metavm.object.instance.core.Id;

public class ByTypeQuery {
    private Id typeId;
    private Id startId;
    private long limit;

    public ByTypeQuery(Id typeId, Id startId, long limit) {
        this.typeId = typeId;
        this.startId = startId;
        this.limit = limit;
    }

    public Id getTypeId() {
        return typeId;
    }

    public void setTypeId(Id typeId) {
        this.typeId = typeId;
    }

    public Id getStartId() {
        return startId;
    }

    public void setStartId(Id startId) {
        this.startId = startId;
    }

    public long getLimit() {
        return limit;
    }

    public void setLimit(long limit) {
        this.limit = limit;
    }
}
