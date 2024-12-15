package org.metavm.object.type;

import org.metavm.api.Entity;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.ValueElement;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.Value;
import org.metavm.util.MvInput;
import org.metavm.util.MvOutput;
import org.metavm.util.WireTypes;

import java.util.Objects;

@Entity
public class FieldRef extends ValueElement implements PropertyRef {

    public static FieldRef read(MvInput input) {
        var classType = (ClassType) Type.readType(input);
        var field = input.getField(input.readId());
        return new FieldRef(classType, field);
    }

    private final ClassType declaringType;
    private final Field rawField;

    public FieldRef(ClassType declaringType, Field rawField) {
        this.declaringType = declaringType;
        this.rawField = rawField;
    }

    public ClassType getDeclaringType() {
        return declaringType;
    }

    public Field getRawField() {
        return rawField;
    }

    @Override
    protected boolean equals0(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof FieldRef fieldRef)) return false;
        return Objects.equals(declaringType, fieldRef.declaringType) && Objects.equals(rawField, fieldRef.rawField);
    }

    @Override
    public int hashCode() {
        return Objects.hash(declaringType, rawField);
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitFieldRef(this);
    }

    @Override
    protected String toString0() {
        return declaringType.getTypeDesc() + "." + rawField.getName();
    }

    public void write(MvOutput output) {
        output.write(WireTypes.FIELD_REF);
        declaringType.write(output);
        output.writeEntityId(rawField);
    }

    public Type getType() {
        return declaringType.getTypeMetadata().getType(rawField.getTypeIndex());
    }

    @Override
    public Property getProperty() {
        return rawField;
    }

    public Value getDefaultValue() {
        return rawField.getDefaultValue();
    }

    public String getName() {
        return rawField.getName();
    }

    public Id getFieldId() {
        return rawField.getId();
    }

    public boolean isStatic() {
        return rawField.isStatic();
    }

    public boolean isTransient() {
        return rawField.isTransient();
    }

    public boolean isReadonly() {
        return rawField.isReadonly();
    }

    public boolean isPublic() {
        return rawField.isPublic();
    }
}
