package tech.metavm.object.meta;

import tech.metavm.entity.EntityField;
import tech.metavm.entity.EntityType;
import tech.metavm.object.instance.ClassInstance;
import tech.metavm.object.instance.persistence.IndexItemPO;
import tech.metavm.object.instance.persistence.IndexKeyPO;
import tech.metavm.object.instance.query.EvaluationContext;
import tech.metavm.object.instance.query.InstanceEvaluationContext;
import tech.metavm.object.meta.rest.dto.ConstraintDTO;
import tech.metavm.util.NncUtils;
import tech.metavm.util.Table;

import java.util.List;

@EntityType("唯一约束")
public class UniqueConstraintRT extends ConstraintRT<UniqueConstraintParam> {

    @EntityField("唯一约束项")
    private Table<UniqueConstraintItem> items;

    public UniqueConstraintRT(ConstraintDTO constraintDTO, UniqueConstraintParam param, ClassType type) {
        super(ConstraintKind.UNIQUE, type, constraintDTO.message());
        setParam(param);
    }

    public UniqueConstraintRT(ClassType type, List<Field> fields, String message) {
        super(ConstraintKind.UNIQUE, type, message);
        this.items = new Table<>(
                UniqueConstraintItem.class,
                NncUtils.map(fields, field -> UniqueConstraintItem.createFieldItem(this, field))
        );
    }

    public void setParam(UniqueConstraintParam param) {
        items = new Table<>(
                UniqueConstraintItem.class,
                NncUtils.map(
                        param.items(),
                        item -> new UniqueConstraintItem(this, item.name(), item.value())
                )
        );
    }

    public List<Field> getFields() {
        return NncUtils.map(items, UniqueConstraintItem::getField);
    }

    public IndexItemPO getKey(long tenantId, ClassInstance instance) {
        EvaluationContext evaluationContext = new InstanceEvaluationContext(instance);
        return new IndexItemPO(
                tenantId,
                getId(),
                NncUtils.map(items, item -> IndexKeyPO.getIndexColumn(item.getValue().evaluate(evaluationContext))),
                instance.getId()
        );
    }

    public boolean containsNull(IndexKeyPO key) {
        return key.containsNull(items.size());
    }

    private String getKeyItem(Field field, ClassInstance instance) {
        return instance.getIndexValue(field);
    }

    @Override
    public String getDefaultMessage() {
        return "唯一属性'" + NncUtils.join(items, UniqueConstraintItem::getName) + "'重复";
    }

    @Override
    protected UniqueConstraintParam getParam(boolean forPersistence) {
        return new UniqueConstraintParam(
                NncUtils.map(items, item -> item.toDTO(forPersistence))
        );
    }

    @Override
    public String getDesc() {
        return "唯一属性(" + NncUtils.join(items, UniqueConstraintItem::getName) + ")";
    }

}
