package tech.metavm.object.meta;

import tech.metavm.entity.Entity;
import tech.metavm.object.meta.persistence.ConstraintPO;
import tech.metavm.object.meta.rest.dto.ConstraintDTO;
import tech.metavm.util.NncUtils;

public abstract class ConstraintRT<T> extends Entity {

    private final Type type;
    private final ConstraintKind kind;

    public ConstraintRT(ConstraintPO po, Type type) {
        super(po.getId(), type.getContext());
        kind = ConstraintKind.getByCode(po.getKind());
        this.type = type;
    }

    public ConstraintRT(ConstraintKind kind, Type type) {
        super(type.getContext());
        this.kind = kind;
        this.type = type;
        type.addConstraint(this);
    }

    protected abstract T getParam(boolean forPersistence);

    protected abstract void setParam(T param);

    public ConstraintDTO toDTO() {
        return new ConstraintDTO(
                getId(),
                kind.code(),
                type.getId(),
                getParam(false)
        );
    }

    public abstract String getDesc();

    public Type getType() {
        return type;
    }

    public ConstraintPO toPO() {
        return new ConstraintPO(
                getId(),
                type.getId(),
                kind.code(),
                NncUtils.toJSONString(getParam(true))
        );
    }

    public void update(ConstraintDTO constraintDTO) {
        setParam(constraintDTO.getParam());
    }
}
