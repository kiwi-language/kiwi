package org.metavm.object.type;

import org.metavm.api.ChildEntity;
import org.metavm.api.EntityType;
import org.metavm.entity.*;
import org.metavm.expression.EvaluationContext;
import org.metavm.expression.InstanceEvaluationContext;
import org.metavm.flow.Flows;
import org.metavm.flow.KlassInput;
import org.metavm.flow.KlassOutput;
import org.metavm.flow.Method;
import org.metavm.object.instance.IndexKeyRT;
import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.Value;
import org.metavm.util.ContextUtil;
import org.metavm.util.InternalException;
import org.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static java.util.Objects.requireNonNull;

@EntityType
public class Index extends Constraint implements LocalKey, ITypeDef {

    @ChildEntity
    private final ChildArray<IndexField> fields = addChild(new ChildArray<>(IndexField.class), "fields");
    private boolean unique;
    private @Nullable Method method;
    private transient IndexDef<?> indexDef;

    public Index(Klass type, String name, String message, boolean unique, List<Field> fields,
                 @Nullable Method method) {
        super(type, name, message);
        this.unique = unique;
        this.method = method;
        for (Field field : fields) {
            IndexField.createFieldItem(this, field);
        }
    }

    public Index(Long tmpId, Klass type, String name, String message, boolean unique) {
        super(type, name, message);
        setTmpId(tmpId);
        this.unique = unique;
    }

    void addField(IndexField item) {
        this.fields.addChild(item);
        item.setIndex(this);
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
        List<Value> instanceValues = new ArrayList<>();
        NncUtils.biForEach(
                fields, values,
                (item, fieldValue) -> instanceValues.add(item.convertEntityToInstance(fieldValue, entityContext))
        );
        return createIndexKey(instanceValues);
    }

    public IndexKeyRT createIndexKey(List<Value> values) {
        NncUtils.requireTrue(values.size() <= fields.size());
        return createIndexKey(NncUtils.zip(fields.subList(0, values.size()), values));
    }

    public IndexKeyRT createIndexKey(Map<IndexField, Value> values) {
        return new IndexKeyRT(this, values);
    }

    public boolean isUnique() {
        return unique;
    }

    @Override
    public String getDefaultMessage() {
        return "Duplicate field '" + NncUtils.join(fields, IndexField::getQualifiedName) + "'";
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
        fields.forEach(f -> f.setIndex(this));
    }

    public IndexRef getRef() {
        return new IndexRef(getDeclaringType().getType(), this);
    }

    @Override
    public void write(KlassOutput output) {
        output.writeEntityId(this);
        output.writeUTF(getName());
        output.writeInt(fields.size());
        for (IndexField field : fields) {
            field.write(output);
        }
        output.writeBoolean(unique);
        output.writeEntityId(Objects.requireNonNull(method));
    }

    @Override
    public void read(KlassInput input) {
        setName(input.readUTF());
        int fieldCount = input.readInt();
        var fields = new ArrayList<IndexField>(fieldCount);
        for (int i = 0; i < fieldCount; i++) {
            fields.add(input.readIndexField());
        }
        setFields(fields);
        unique = input.readBoolean();
        method = input.getMethod(input.readId());
    }

    public void setMethod(@Nullable Method method) {
        this.method = method;
    }

    public @Nullable Method getMethod() {
        return method;
    }
}
