package org.metavm.system.persistence;

public class RangeInc {
    private long id;
    private long inc;
    private boolean deactivating;

    public RangeInc() {
    }

    public RangeInc(long id, long inc, boolean deactivating) {
        this.id = id;
        this.inc = inc;
        this.deactivating = deactivating;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getInc() {
        return inc;
    }

    public void setInc(long inc) {
        this.inc = inc;
    }

    public boolean isDeactivating() {
        return deactivating;
    }

    public void setDeactivating(boolean deactivating) {
        this.deactivating = deactivating;
    }
}
