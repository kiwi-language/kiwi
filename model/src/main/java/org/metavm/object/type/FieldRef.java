package org.metavm.object.type;

import org.metavm.api.EntityType;
import org.metavm.entity.CopyIgnore;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.ValueElement;
import org.metavm.util.MvInput;
import org.metavm.util.MvOutput;
import org.metavm.util.WireTypes;

import java.util.Objects;

@EntityType
public class FieldRef extends ValueElement implements PropertyRef {

    public static FieldRef read(MvInput input) {
        var classType = (ClassType) Type.readType(input);
        var field = input.getField(input.readId());
        return new FieldRef(classType, field);
    }

    private final ClassType declaringType;
    private final Field rawField;
    @CopyIgnore
    private transient Field resolved;

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

    public Field resolve() {
        if(resolved != null)
            return resolved;
        var klass = declaringType.resolve();
//        logger.info("resolving field: " + rawField.getName() + " in klass " + klass);
        return resolved = rawField.isStatic() ?
                klass.findStaticField(f -> f.getEffectiveTemplate() == rawField) :
                Objects.requireNonNull(
                        klass.findField(f -> f.getEffectiveTemplate() == rawField),
                        () -> "Cannot find field with template " + rawField.getQualifiedName() + " in klass " + klass.getTypeDesc());
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
}
