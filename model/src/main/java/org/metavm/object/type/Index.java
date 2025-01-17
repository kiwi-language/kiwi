package org.metavm.object.type;

import org.metavm.annotation.NativeEntity;
import org.metavm.api.Entity;
import org.metavm.api.Generated;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.EntityRegistry;
import org.metavm.entity.IndexDef;
import org.metavm.entity.LocalKey;
import org.metavm.flow.CodeWriter;
import org.metavm.flow.Method;
import org.metavm.object.instance.IndexKeyRT;
import org.metavm.object.instance.core.*;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.type.ClassType;
import org.metavm.object.type.Klass;
import org.metavm.util.*;
import org.metavm.util.MvInput;
import org.metavm.util.MvOutput;
import org.metavm.util.StreamVisitor;

import javax.annotation.Nullable;
import java.util.*;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;

@NativeEntity(36)
@Entity
public class Index extends Constraint implements LocalKey, ITypeDef {

    @SuppressWarnings("unused")
    private static Klass __klass__;
    private List<IndexField> fields = new ArrayList<>();
    private boolean unique;
    private @Nullable Reference methodReference;
    private @Nullable Reference method;
    private transient IndexDef<?> indexDef;

    public Index(Klass type, String name, String message, boolean unique, List<Field> fields,
                 @Nullable Method method) {
        super(type, name, message);
        this.unique = unique;
        this.method = Utils.safeCall(method, Instance::getReference);
        for (Field field : fields) {
            IndexField.createFieldItem(this, field);
        }
    }

    public Index(Long tmpId, Klass type, String name, String message, boolean unique) {
        super(type, name, message);
        setTmpId(tmpId);
        this.unique = unique;
    }

    @Generated
    public static void visitBody(StreamVisitor visitor) {
        Constraint.visitBody(visitor);
        visitor.visitList(visitor::visitEntity);
        visitor.visitBoolean();
        visitor.visitNullable(visitor::visitValue);
        visitor.visitNullable(visitor::visitValue);
    }

    void addField(IndexField item) {
        this.fields.add(item);
        item.setIndex(this);
    }

    public IndexField getField(Id id) {
        return Utils.findRequired(fields, f -> f.idEquals(id), () -> "Can not find index item for id " + id);
    }

    public @Nullable IndexField findField(Predicate<IndexField> predicate) {
        return Utils.find(fields, predicate);
    }

    public IndexField getFieldByTypeField(Field field) {
        return Utils.findRequired(
                fields,
                item -> Objects.equals(item.getField(), field)
        );
    }

    public List<Field> getTypeFields() {
        return Utils.map(fields, IndexField::getField);
    }

    public IndexKeyRT createIndexKey(List<Value> values) {
        Utils.require(values.size() <= fields.size());
        return createIndexKey(Utils.zip(fields.subList(0, values.size()), values));
    }

    public IndexKeyRT createIndexKey(Map<IndexField, Value> values) {
        return new IndexKeyRT(this, values);
    }

    public boolean isUnique() {
        return unique;
    }

    @Override
    public String getDefaultMessage() {
        return "Duplicate field '" + Utils.join(fields, IndexField::getQualifiedName) + "'";
    }

    public List<IndexField> getFields() {
        return Collections.unmodifiableList(fields);
    }

    public int getNumFields() {
        return fields.size();
    }

    public boolean isFieldIndex(Field field) {
        if (fields.size() != 1) {
            return false;
        }
        IndexField indexField = fields.getFirst();
        return Objects.equals(indexField.getField(), field);
    }

    @Override
    public String getDesc() {
        return "Index(" + Utils.join(fields, IndexField::getName) + ")";
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
    public String getTitle() {
        return getName();
    }

    public void setFields(List<IndexField> fields) {
        this.fields.clear();
        this.fields.addAll(fields);
        fields.forEach(f -> f.setIndex(this));
    }

    public IndexRef getRef() {
        return new IndexRef(getDeclaringType().getType(), this);
    }


    public void setMethod(@Nullable Method method) {
        this.method = Utils.safeCall(method, Instance::getReference);
    }

    public @Nullable Method getMethod() {
        return Utils.safeCall(method, m -> (Method) m.get());
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitIndex(this);
    }

    @Override
    public void acceptChildren(ElementVisitor<?> visitor) {
        super.acceptChildren(visitor);
        fields.forEach(arg -> arg.accept(visitor));
    }

    @Override
    public void forEachReference(Consumer<Reference> action) {
        super.forEachReference(action);
        fields.forEach(arg -> action.accept(arg.getReference()));
        if (methodReference != null) action.accept(methodReference);
        if (method != null) action.accept(method);
    }

    @Override
    public void buildJson(Map<String, Object> map) {
        map.put("typeFields", this.getTypeFields().stream().map(org.metavm.entity.Entity::getStringId).toList());
        map.put("unique", this.isUnique());
        map.put("defaultMessage", this.getDefaultMessage());
        map.put("fields", this.getFields().stream().map(org.metavm.entity.Entity::getStringId).toList());
        map.put("numFields", this.getNumFields());
        map.put("desc", this.getDesc());
        map.put("indexDef", this.getIndexDef());
        map.put("ref", this.getRef().toJson());
        var method = this.getMethod();
        if (method != null) map.put("method", method.getStringId());
        var message = this.getMessage();
        if (message != null) map.put("message", message);
        map.put("name", this.getName());
        map.put("qualifiedName", this.getQualifiedName());
        map.put("declaringType", this.getDeclaringType().getStringId());
    }

    @Override
    public Klass getInstanceKlass() {
        return __klass__;
    }

    @Override
    public ClassType getInstanceType() {
        return __klass__.getType();
    }

    @Override
    public void forEachChild(Consumer<? super Instance> action) {
        super.forEachChild(action);
        fields.forEach(action);
    }

    @Override
    public int getEntityTag() {
        return EntityRegistry.TAG_Index;
    }

    @Generated
    @Override
    public void readBody(MvInput input, org.metavm.entity.Entity parent) {
        super.readBody(input, parent);
        this.fields = input.readList(() -> input.readEntity(IndexField.class, this));
        this.unique = input.readBoolean();
        this.methodReference = input.readNullable(() -> (Reference) input.readValue());
        this.method = input.readNullable(() -> (Reference) input.readValue());
    }

    @Generated
    @Override
    public void writeBody(MvOutput output) {
        super.writeBody(output);
        output.writeList(fields, output::writeEntity);
        output.writeBoolean(unique);
        output.writeNullable(methodReference, output::writeValue);
        output.writeNullable(method, output::writeValue);
    }

    @Override
    protected void buildSource(Map<String, Value> source) {
        super.buildSource(source);
    }

    public void writeCode(CodeWriter writer) {
        writer.writeln((unique ? "unique index " : "index ") + getName());
    }
}
