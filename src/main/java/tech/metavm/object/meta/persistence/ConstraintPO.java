package tech.metavm.object.meta.persistence;

import tech.metavm.entity.EntityPO;
import tech.metavm.entity.IndexDef;
import tech.metavm.object.meta.ConstraintRT;
import tech.metavm.util.TypeReference;

public class ConstraintPO extends EntityPO {

    public static final IndexDef<ConstraintRT<?>> INDEX_DECLARING_TYPE_ID =
            new IndexDef<>(new TypeReference<>() {}, "typeId");

    private Long id;
    private Integer kind;
    private Long declaringTypeId;
    private String message;
    private String param;

    public ConstraintPO() {
    }

    public ConstraintPO(Long id, Long declaringTypeId, Integer kind, String message, String param) {
        this.id = id;
        this.declaringTypeId = declaringTypeId;
        this.kind = kind;
        this.message = message;
        this.param = param;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getDeclaringTypeId() {
        return declaringTypeId;
    }

    public void setDeclaringTypeId(Long declaringTypeId) {
        this.declaringTypeId = declaringTypeId;
    }

    public Integer getKind() {
        return kind;
    }

    public void setKind(Integer kind) {
        this.kind = kind;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getParam() {
        return param;
    }

    public void setParam(String param) {
        this.param = param;
    }
}
