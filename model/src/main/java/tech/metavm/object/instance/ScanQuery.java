package tech.metavm.object.instance;

public class ScanQuery {
    private long startId;
    private long limit;

    public ScanQuery(long startId, long limit) {
        this.startId = startId;
        this.limit = limit;
    }

    public ScanQuery() {
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
