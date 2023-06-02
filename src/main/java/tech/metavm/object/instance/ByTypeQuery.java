package tech.metavm.object.instance;

public class ByTypeQuery {
    private long typeId;
    private long startId;
    private long limit;

    public ByTypeQuery(long typeId, long startId, long limit) {
        this.typeId = typeId;
        this.startId = startId;
        this.limit = limit;
    }

    public ByTypeQuery() {
        this(0L, 0L, 0L);
    }

    public long getTypeId() {
        return typeId;
    }

    public void setTypeId(long typeId) {
        this.typeId = typeId;
    }

    public long getStartId() {
        return startId;
    }

    public void setStartId(long startId) {
        this.startId = startId;
    }

    public long getLimit() {
        return limit;
    }

    public void setLimit(long limit) {
        this.limit = limit;
    }
}
