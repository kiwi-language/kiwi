package tech.metavm.object.instance.persistence;

import java.util.Objects;

public final class InstanceTitlePO {
    private final long id;
    private final String title;

    public InstanceTitlePO(
            long id,
            String title
    ) {
        this.id = id;
        this.title = title;
    }

    public long id() {
        return id;
    }

    public String title() {
        return title;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (InstanceTitlePO) obj;
        return this.id == that.id &&
                Objects.equals(this.title, that.title);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title);
    }

    @Override
    public String toString() {
        return "InstanceTitlePO[" +
                "id=" + id + ", " +
                "title=" + title + ']';
    }

}
