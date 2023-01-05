package tech.metavm.object.meta;

import org.checkerframework.common.aliasing.qual.Unique;
import tech.metavm.entity.*;
import tech.metavm.flow.ReferenceValue;
import tech.metavm.flow.ValueFactory;
import tech.metavm.flow.rest.ValueDTO;
import tech.metavm.object.instance.ClassInstance;
import tech.metavm.object.instance.persistence.IndexItemPO;
import tech.metavm.object.instance.persistence.IndexKeyPO;
import tech.metavm.object.instance.query.EvaluationContext;
import tech.metavm.object.instance.query.ExpressionUtil;
import tech.metavm.object.instance.query.InstanceEvaluationContext;
import tech.metavm.object.instance.query.TypeParsingContext;
import tech.metavm.object.meta.rest.dto.ConstraintDTO;
import tech.metavm.util.NncUtils;
import tech.metavm.util.ReflectUtils;
import tech.metavm.util.Table;

import java.util.List;

@EntityType("唯一约束")
public class IndexConstraintRT extends ConstraintRT<UniqueConstraintParam> {

    public static IndexConstraintRT createByIndexDef(IndexDef<?> indexDef, DefContext defContext) {
        Class<?> javaType = indexDef.getType();
        ClassType type = defContext.getClassType(indexDef.getType());
        List<Field> fields = NncUtils.map(
                indexDef.getFieldNames(),
                fieldName -> type.getFieldByJavaField(ReflectUtils.getField(javaType, fieldName))
        );
        IndexConstraintRT index = new IndexConstraintRT(type, indexDef.isUnique(), null);
        for (Field field : fields) {
            new IndexConstraintItem(
                    index,
                    field.getName(),
                    new ReferenceValue(ExpressionUtil.fieldExpr(field))
            );
        }
        return index;
    }

    @EntityField("唯一约束项")
    private final Table<IndexConstraintItem> items;
    private final boolean unique;
    private transient IndexDef<?> indexDef;

    public IndexConstraintRT(ClassType type, List<Field> fields, boolean unique, String message) {
        super(ConstraintKind.UNIQUE, type, message);
        this.unique = unique;
        this.items = new Table<>(IndexConstraintItem.class);
        for (Field field : fields) {
            IndexConstraintItem.createFieldItem(this, field);
        }
    }

    public IndexConstraintRT(ClassType type, boolean unique, String message) {
        super(ConstraintKind.UNIQUE, type, message);
        this.items = new Table<>(IndexConstraintItem.class);
        this.unique = unique;
    }

    void addItem(IndexConstraintItem item) {
        this.items.add(item);
    }

    public IndexConstraintItem getItem(long id) {
        return NncUtils.requireNonNull(
                items.get(Entity::getId, id),
                "Can not find index item for id " + id
        );
    }

    public void setParam(UniqueConstraintParam param) {
    }

    public List<Field> getFields() {
        return NncUtils.map(items, IndexConstraintItem::getField);
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

    public boolean isUnique() {
        return unique;
    }

    @Override
    public String getDefaultMessage() {
        return "唯一属性'" + NncUtils.join(items, IndexConstraintItem::getName) + "'重复";
    }

    @Override
    protected UniqueConstraintParam getParam(boolean forPersistence) {
        return new UniqueConstraintParam(
                NncUtils.map(items, item -> item.toDTO(forPersistence))
        );
    }

    public Table<IndexConstraintItem> getItems() {
        return items;
    }

    @Override
    public String getDesc() {
        return "唯一属性(" + NncUtils.join(items, IndexConstraintItem::getName) + ")";
    }

    public IndexDef<?> getIndexDef() {
        return indexDef;
    }

    public void setIndexDef(IndexDef<?> indexDef) {
        this.indexDef = indexDef;
    }
}
