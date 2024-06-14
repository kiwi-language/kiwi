package org.metavm.system.persistence;


public class BlockPO {
    private Long id;
    private Long appId;
    private Integer typeTag;
    private Long typeId;
    private Long startId;
    private Long endId;
    private Long nextId;
    private Boolean active;

    public BlockPO() {
    }

    public BlockPO(Long id, Long appId, Integer typeTag, Long typeId, Long startId, Long endId, Long nextId, Boolean active) {
        this.id = id;
        this.appId = appId;
        this.typeTag = typeTag;
        this.typeId = typeId;
        this.startId = startId;
        this.endId = endId;
        this.nextId = nextId;
        this.active = active;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAppId() {
        return appId;
    }

    public void setAppId(Long appId) {
        this.appId = appId;
    }

    public Integer getTypeTag() {
        return typeTag;
    }

    public void setTypeTag(Integer typeTag) {
        this.typeTag = typeTag;
    }

    public Long getTypeId() {
        return typeId;
    }

    public void setTypeId(Long typeId) {
        this.typeId = typeId;
    }

    public Long getStartId() {
        return startId;
    }

    public void setStartId(Long startId) {
        this.startId = startId;
    }

    public Long getEndId() {
        return endId;
    }

    public void setEndId(Long endId) {
        this.endId = endId;
    }

    public Long getNextId() {
        return nextId;
    }

    public void setNextId(Long nextId) {
        this.nextId = nextId;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public BlockPO copy() {
        return new BlockPO(id, appId, typeTag, typeId, startId, endId, nextId, active);
    }

    @Override
    public String toString() {
        return "IdRange{" +
                "id=" + id +
                ", appId=" + appId +
                ", typeId=" + typeId +
                ", start=" + startId +
                ", end=" + endId +
                ", next=" + nextId +
                ", active=" + active +
                '}';
    }
}