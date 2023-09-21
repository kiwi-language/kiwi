package tech.metavm.object.meta;

import tech.metavm.entity.*;
import tech.metavm.object.instance.ClassInstance;
import tech.metavm.object.instance.IndexKeyRT;
import tech.metavm.object.instance.Instance;
import tech.metavm.object.instance.persistence.IndexEntryPO;
import tech.metavm.object.instance.persistence.IndexKeyPO;
import tech.metavm.expression.EvaluationContext;
import tech.metavm.expression.InstanceEvaluationContext;
import tech.metavm.util.InternalException;
import tech.metavm.util.NncUtils;
import tech.metavm.util.Table;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@EntityType("索引")
public class Index extends Constraint<UniqueConstraintParamDTO> {

    @ChildEntity("索引字段列表")
    private final Table<IndexField> fields = new Table<>(IndexField.class, true);
    @EntityField("是否唯一")
    private final boolean unique;
    private transient IndexDef<?> indexDef;

    public Index(ClassType type, List<Field> fields, boolean unique, String message) {
        super(ConstraintKind.UNIQUE, type, message);
        this.unique = unique;
        for (Field field : fields) {
            IndexField.createFieldItem(this, field);
        }
    }

    public Index(ClassType type, boolean unique, String message) {
        super(ConstraintKind.UNIQUE, type, message);
        this.unique = unique;
    }

    void addField(IndexField item) {
        this.fields.add(item);
    }

    public IndexField getField(long id) {
        return NncUtils.requireNonNull(
                fields.get(Entity::getId, id),
                "Can not find index item for id " + id
        );
    }

    public IndexField getFieldByTypeField(Field field) {
        return NncUtils.findRequired(
                fields,
                item -> Objects.equals(item.getField(), field)
        );
    }

    public List<Field> getTypeFields() {
        return NncUtils.map(fields, IndexField::getField);
    }

    public IndexEntryPO createIndexEntry(long tenantId, ClassInstance instance) {
        IndexKeyRT key = createIndexKey(instance);
        return new IndexEntryPO(tenantId, key.toPO(), instance.getIdRequired());
    }

    public IndexKeyRT createIndexKeyByModels(List<Object> values, IEntityContext entityContext) {
        NncUtils.requireEquals(fields.size(), values.size());
        List<Instance> instanceValues = new ArrayList<>();
        NncUtils.biForEach(
                fields, values,
                (item, fieldValue) -> instanceValues.add(item.convertModelToInstance(fieldValue, entityContext))
        );
        return createIndexKey(instanceValues);
    }

    public IndexKeyRT createIndexKey(ClassInstance instance) {
        EvaluationContext evaluationContext = new InstanceEvaluationContext(instance);
        List<Instance> values = NncUtils.map(fields, field -> field.getValue().evaluate(evaluationContext));
        return createIndexKey(values);
    }

    public IndexKeyRT createIndexKey(List<Instance> values) {
        NncUtils.requireEquals(fields.size(), values.size());
        IndexKeyPO key = new IndexKeyPO();
        key.setConstraintId(NncUtils.requireNonNull(getId()));
        var fieldValues = NncUtils.zip(fields, values);
        return new IndexKeyRT(this, fieldValues);
    }

    public boolean containsNull(IndexKeyPO key) {
        return NncUtils.anyMatch(fields, item -> item.isItemNull(key));
    }

    public boolean isUnique() {
        return unique;
    }

    @Override
    public String getDefaultMessage() {
        return "唯一属性'" + NncUtils.join(fields, IndexField::getName) + "'重复";
    }

    @Override
    protected UniqueConstraintParamDTO getParam(boolean forPersistence) {
        return new UniqueConstraintParamDTO(
                NncUtils.map(fields, item -> item.toDTO(forPersistence))
        );
    }

    public Table<IndexField> getFields() {
        return fields;
    }

    public boolean isFieldIndex(Field field) {
        if(fields.size() != 1) {
            return false;
        }
        IndexField indexField = fields.get(0);
        return Objects.equals(indexField.getField(), field);
    }

    @Override
    public String getDesc() {
        return "唯一属性(" + NncUtils.join(fields, IndexField::getName) + ")";
    }

    public IndexDef<?> getIndexDef() {
        return indexDef;
    }

    public void setIndexDef(IndexDef<?> indexDef) {
        this.indexDef = indexDef;
    }

    public boolean isLastItem(IndexField item) {
        return !fields.isEmpty() && fields.get(fields.size()-1) == item;
    }

    public int getItemIndex(IndexField item) {
        int index =  fields.indexOf(item);
        if(index < 0) {
            throw new InternalException(item + " is not contained in " + this);
        }
        return index;
    }

}
