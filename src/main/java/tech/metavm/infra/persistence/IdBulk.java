package tech.metavm.infra.persistence;

public class IdBulk {

    private Integer num;
    private Long nextId;

    public IdBulk() {}

    public IdBulk(Integer num, Long nextId) {
        this.num = num;
        this.nextId = nextId;
    }

    public Integer getNum() {
        return num;
    }

    public void setNum(Integer num) {
        this.num = num;
    }

    public Long getNextId() {
        return nextId;
    }

    public void setNextId(Long nextId) {
        this.nextId = nextId;
    }
}