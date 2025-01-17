package org.metavm.object.type;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.metavm.api.Entity;
import org.metavm.entity.ElementVisitor;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.instance.core.Value;
import org.metavm.util.MvInput;
import org.metavm.util.MvOutput;
import org.metavm.util.WireTypes;

import java.util.Objects;
import java.util.function.Consumer;

@Entity
@Slf4j
public class FieldRef implements PropertyRef {

    @SuppressWarnings("unused")
    private static Klass __klass__;

    public static FieldRef read(MvInput input) {
        var classType = (ClassType) input.readType();
        var fieldReference = input.readReference();
        return new FieldRef(classType, fieldReference);
    }

    private final ClassType declaringType;
    private final Reference fieldReference;
//    public Field rawField;

    public FieldRef(ClassType declaringType, @NotNull Field rawField) {
        this.declaringType = declaringType;
//        this.rawField = rawField;
        fieldReference = rawField.getReference();
    }

    private FieldRef(ClassType declaringType, Reference fieldReference) {
        this.declaringType = declaringType;
        this.fieldReference = fieldReference;
    }

    public ClassType getDeclaringType() {
        return declaringType;
    }

    public Field getRawField() {
        return (Field) fieldReference.get();
    }

    public Reference getFieldReference() {
        return fieldReference;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof FieldRef fieldRef)) return false;
        return Objects.equals(declaringType, fieldRef.declaringType) && Objects.equals(fieldReference, fieldRef.fieldReference);
    }

    @Override
    public int hashCode() {
        return Objects.hash(declaringType, fieldReference);
    }

    @Override
    public String toString() {
        return declaringType.getTypeDesc() + "." + getRawField().getName();
    }

    public void write(MvOutput output) {
        output.write(WireTypes.FIELD_REF);
        declaringType.write(output);
        output.writeReference(fieldReference);
    }

    public Type getPropertyType() {
        return declaringType.getTypeMetadata().getType(getRawField().getTypeIndex());
    }

    @Override
    public Property getProperty() {
        return getRawField();
    }

    public Value getDefaultValue() {
        return getRawField().getDefaultValue();
    }

    public String getName() {
        return getRawField().getName();
    }

    public Id getFieldId() {
        return fieldReference.getId();
    }

    public boolean isStatic() {
        return getRawField().isStatic();
    }

    public boolean isTransient() {
        return getRawField().isTransient();
    }

    public boolean isReadonly() {
        return getRawField().isReadonly();
    }

    public boolean isPublic() {
        return getRawField().isPublic();
    }

    public boolean isChild() {
        return getRawField().isChild();
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitFieldRef(this);
    }

    @Override
    public ClassType getValueType() {
        return __klass__.getType();
    }

    @Override
    public void acceptChildren(ElementVisitor<?> visitor) {
        declaringType.accept(visitor);
    }

    public void forEachReference(Consumer<Reference> action) {
        declaringType.forEachReference(action);
        action.accept(fieldReference);
    }
}
