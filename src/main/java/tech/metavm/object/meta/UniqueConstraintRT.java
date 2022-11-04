package tech.metavm.object.meta;

import tech.metavm.entity.Entity;
import tech.metavm.object.instance.Instance;
import tech.metavm.object.instance.persistence.IndexItemPO;
import tech.metavm.object.meta.persistence.ConstraintPO;
import tech.metavm.object.meta.rest.dto.ConstraintDTO;
import tech.metavm.util.NncUtils;

import java.util.ArrayList;
import java.util.List;

public class UniqueConstraintRT extends ConstraintRT<UniqueConstraintParam> {

    private List<Field> fields;

    public UniqueConstraintRT(ConstraintPO po, UniqueConstraintParam param, Type type) {
        super(po, type);
        setParam(param);
    }

    public UniqueConstraintRT(ConstraintDTO constraintDTO, UniqueConstraintParam param, Type type) {
        super(ConstraintKind.UNIQUE, type);
        setParam(param);
    }

    public UniqueConstraintRT(List<Field> fields, Type type) {
        super(ConstraintKind.UNIQUE, type);
        this.fields = new ArrayList<>(fields);
    }

    public void setParam(UniqueConstraintParam param) {
        fields = new ArrayList<>(NncUtils.map(param.fieldIds(), context::getFieldRef));
    }

    public List<Field> getFields() {
        return fields;
    }

    public IndexItemPO getKey(Instance instance) {
        return new IndexItemPO(
                getTenantId(),
                getId(),
                NncUtils.map(fields, f -> getKeyItem(f, instance)),
                instance.getId()
        );
    }

    private String getKeyItem(Field field, Instance instance) {
        return instance.getIndexValue(field);
    }

    @Override
    protected UniqueConstraintParam getParam(boolean forPersistence) {
        return new UniqueConstraintParam(
                NncUtils.map(fields, Entity::getId)
        );
    }

    @Override
    public String getDesc() {
        return "唯一属性(" + NncUtils.join(fields, Field::getName) + ")";
    }
}
