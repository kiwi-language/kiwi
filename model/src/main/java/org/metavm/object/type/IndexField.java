package org.metavm.object.type;

import org.jetbrains.annotations.NotNull;
import org.metavm.annotation.NativeEntity;
import org.metavm.api.Entity;
import org.metavm.api.Generated;
import org.metavm.entity.BuildKeyContext;
import org.metavm.entity.Element;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.EntityRegistry;
import org.metavm.entity.LocalKey;
import org.metavm.expression.Expression;
import org.metavm.expression.PropertyExpression;
import org.metavm.expression.ThisExpression;
import org.metavm.flow.Value;
import org.metavm.flow.Values;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.InstanceVisitor;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.type.ClassType;
import org.metavm.object.type.Klass;
import org.metavm.util.MvInput;
import org.metavm.util.MvOutput;
import org.metavm.util.StreamVisitor;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

@NativeEntity(0)
@Entity
public class IndexField extends org.metavm.entity.Entity implements LocalKey, ITypeDef, Element {

    @SuppressWarnings("unused")
    private static Klass __klass__;

    public static IndexField createFieldItem(Index constraint, Field field) {
        return new IndexField(
                constraint,
                field.getName(),
                field.getType(),
                Values.property(field)
        );
    }

    private Index index;
    private String name;
    private int typeIndex;
    @Nullable
    private Value value;

    public IndexField(Index index, String name, Type type, @Nullable Value value) {
        this(index, name, index.getDeclaringType().addConstant(type), value);
    }

    public IndexField(Index index, String name, int typeIndex, @Nullable Value value) {
        setName(name);
        this.typeIndex = typeIndex;
        this.index = index;
        this.value = value;
        index.addField(this);
    }

    @Generated
    public static void visitBody(StreamVisitor visitor) {
        visitor.visitUTF();
        visitor.visitInt();
        visitor.visitNullable(() -> Value.visit(visitor));
    }

    public String getName() {
        return name;
    }

    public Type getType(TypeMetadata typeMetadata) {
        return typeMetadata.getType(typeIndex);
    }

    public Type getType() {
        return index.getDeclaringType().getConstantPool().getType(typeIndex);
    }

    public int getTypeIndex() {
        return typeIndex;
    }

    public String getQualifiedName() {
        return index.getDeclaringType().getName() + "." + index.getName() + "." + name;
    }

    @Nullable
    public org.metavm.flow.Value getValue() {
        return value;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setValue(@Nullable org.metavm.flow.Value value) {
        this.value = value;
    }

    public Index getIndex() {
        return index;
    }

    public Field getField() {
        return extractField(Objects.requireNonNull(value).getExpression());
    }

    private Field extractField(Expression expression) {
        if (expression instanceof PropertyExpression fieldExpression) {
            if ((fieldExpression.getInstance() instanceof ThisExpression)) {
                return (Field) fieldExpression.getProperty();
            }
        }
        return null;
    }

    public int getFieldIndex() {
        return index.getFieldIndex(this);
    }

    @Override
    public boolean isValidLocalKey() {
        return true;
    }

    @Override
    public String getLocalKey(@NotNull BuildKeyContext context) {
        return name;
    }

    @Nullable
    @Override
    public org.metavm.entity.Entity getParentEntity() {
        return index;
    }

    public void setIndex(Index index) {
        this.index = index;
    }

    public void setType(Type type) {
        typeIndex = getIndex().getDeclaringType().addConstant(type);
    }

    public void setTypeIndex(int typeIndex) {
        this.typeIndex = typeIndex;
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitIndexField(this);
    }

    @Override
    public void acceptChildren(ElementVisitor<?> visitor) {
    }

    @Override
    public void forEachReference(Consumer<Reference> action) {
        if (value != null) value.forEachReference(action);
    }

    @Override
    public void buildJson(Map<String, Object> map) {
        map.put("name", this.getName());
        map.put("qualifiedName", this.getQualifiedName());
        map.put("index", this.getIndex().getStringId());
        map.put("field", this.getField().getStringId());
        map.put("fieldIndex", this.getFieldIndex());
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
    }

    @Override
    public int getEntityTag() {
        return EntityRegistry.TAG_IndexField;
    }

    @Generated
    @Override
    public void readBody(MvInput input, org.metavm.entity.Entity parent) {
        this.index = (Index) parent;
        this.name = input.readUTF();
        this.typeIndex = input.readInt();
        this.value = input.readNullable(() -> Value.read(input));
    }

    @Generated
    @Override
    public void writeBody(MvOutput output) {
        output.writeUTF(name);
        output.writeInt(typeIndex);
        output.writeNullable(value, arg0 -> arg0.write(output));
    }

    @Override
    protected void buildSource(Map<String, org.metavm.object.instance.core.Value> source) {
    }
}
