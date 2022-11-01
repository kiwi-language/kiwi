package tech.metavm.object.meta.persistence;

public class ConstraintPO {
    private Long id;
    private Integer kind;
    private Long typeId;
    private String param;

    public ConstraintPO() {
    }

    public ConstraintPO(Long id, Long typeId, Integer kind, String param) {
        this.id = id;
        this.typeId = typeId;
        this.kind = kind;
        this.param = param;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTypeId() {
        return typeId;
    }

    public void setTypeId(Long typeId) {
        this.typeId = typeId;
    }

    public Integer getKind() {
        return kind;
    }

    public void setKind(Integer kind) {
        this.kind = kind;
    }

    public String getParam() {
        return param;
    }

    public void setParam(String param) {
        this.param = param;
    }
}
