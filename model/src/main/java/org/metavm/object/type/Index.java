package org.metavm.object.type;

import org.metavm.api.ChildEntity;
import org.metavm.api.EntityType;
import org.metavm.entity.*;
import org.metavm.expression.EvaluationContext;
import org.metavm.expression.InstanceEvaluationContext;
import org.metavm.object.instance.IndexKeyRT;
import org.metavm.object.instance.core.ArrayInstance;
import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.type.rest.dto.IndexFieldDTO;
import org.metavm.object.type.rest.dto.IndexParam;
import org.metavm.util.InternalException;
import org.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

@EntityType
public class Index extends Constraint implements LocalKey {

    @ChildEntity
    private final ChildArray<IndexField> fields = addChild(new ChildArray<>(IndexField.class), "fields");
    private final boolean unique;
    private transient IndexDef<?> indexDef;

    public Index(Klass type, String name, @Nullable String code, String message, boolean unique, List<Field> fields) {
        super(ConstraintKind.UNIQUE, type, name, code, message);
        this.unique = unique;
        for (Field field : fields) {
            IndexField.createFieldItem(this, field);
        }
    }

    public Index(Klass type, String name, @Nullable String code, String message, boolean unique) {
        super(ConstraintKind.UNIQUE, type, name, code, message);
        this.unique = unique;
    }

    void addField(IndexField item) {
        this.fields.addChild(item);
    }

    public IndexField getField(Id id) {
        return NncUtils.requireNonNull(
                fields.get(Entity::tryGetId, id),
                "Can not find index item for id " + id
        );
    }

    public @Nullable IndexField findField(Predicate<IndexField> predicate) {
        return NncUtils.find(fields, predicate);
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

    public IndexKeyRT createIndexKeyByModels(List<Object> values, IEntityContext entityContext) {
        NncUtils.requireEquals(fields.size(), values.size());
        List<Instance> instanceValues = new ArrayList<>();
        NncUtils.biForEach(
                fields, values,
                (item, fieldValue) -> instanceValues.add(item.convertEntityToInstance(fieldValue, entityContext))
        );
        return createIndexKey(instanceValues);
    }

    public List<IndexKeyRT> createIndexKey(ClassInstance instance) {
        var result = new ArrayList<IndexKeyRT>();
        forEachIndexKey(instance, result::add);
        return result;
    }

    public void forEachIndexKey(ClassInstance instance, Consumer<IndexKeyRT> action) {
        EvaluationContext evaluationContext = new InstanceEvaluationContext(instance);
        Map<IndexField, Instance> values = new HashMap<>();
        for (int i = 0; i < fields.size() - 1; i++) {
            var field = fields.get(i);
            values.put(field, field.getValue().evaluate(evaluationContext));
        }
        // When the last index item is an array, create an index key for each element.
        var lastField = fields.get(fields.size() - 1);
        if (lastField.getValue().getType().getUnderlyingType().isArray()) {
            var lastValues = new HashSet<>(((ArrayInstance) lastField.getValue().evaluate(evaluationContext)).getElements());
            List<IndexKeyRT> keys = new ArrayList<>();
            for (Instance lastValue : lastValues) {
                values.put(lastField, lastValue);
                action.accept(createIndexKey(values));
            }
        } else {
            values.put(lastField, lastField.getValue().evaluate(evaluationContext));
            action.accept(new IndexKeyRT(this, values));
        }
    }

    public IndexKeyRT createIndexKey(List<Instance> values) {
        NncUtils.requireTrue(values.size() <= fields.size());
        return createIndexKey(NncUtils.zip(fields.subList(0, values.size()), values));
    }

    public IndexKeyRT createIndexKey(Map<IndexField, Instance> values) {
        return new IndexKeyRT(this, values);
    }

    public boolean isUnique() {
        return unique;
    }

    @Override
    public String getDefaultMessage() {
        return "Duplicate field '" + NncUtils.join(fields, IndexField::getName) + "'";
    }

    @Override
    protected IndexParam getParam() {
        return new IndexParam(
                unique,
                NncUtils.map(fields, item -> item.toDTO())
        );
    }

    @Override
    public void setParam(Object param, IEntityContext context) {
        IndexParam indexParam = (IndexParam) param;
        if (indexParam.fields() != null) {
            for (IndexFieldDTO fieldDTO : indexParam.fields()) {
                if (fieldDTO.id() != null) {
                    var field = fields.get(Entity::getStringId, fieldDTO.id());
                    if (fieldDTO.name() != null)
                        field.setName(fieldDTO.name());
                    if (fieldDTO.code() != null)
                        field.setCode(fieldDTO.code());
                }
            }
        }
    }

    public List<IndexField> getFields() {
        return fields.toList();
    }

    public int getNumFields() {
        return fields.size();
    }

    public boolean isFieldIndex(Field field) {
        if (fields.size() != 1) {
            return false;
        }
        IndexField indexField = fields.get(0);
        return Objects.equals(indexField.getField(), field);
    }

    @Override
    public String getDesc() {
        return "Index(" + NncUtils.join(fields, IndexField::getName) + ")";
    }

    public IndexDef<?> getIndexDef() {
        return indexDef;
    }

    public void setIndexDef(IndexDef<?> indexDef) {
        this.indexDef = indexDef;
    }

    public boolean isLastItem(IndexField item) {
        return !fields.isEmpty() && fields.get(fields.size() - 1) == item;
    }

    public int getFieldIndex(IndexField item) {
        int index = fields.indexOf(item);
        if (index < 0)
            throw new InternalException(item + " is not contained in " + this);
        return index;
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitIndex(this);
    }

    public void setFields(List<IndexField> fields) {
        this.fields.resetChildren(fields);
    }
}
