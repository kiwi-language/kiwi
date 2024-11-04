package org.metavm.object.type;

import org.metavm.api.EntityType;
import org.metavm.entity.*;
import org.metavm.object.instance.core.Id;
import org.metavm.object.type.rest.dto.FieldRefDTO;

import java.util.Objects;

@EntityType
public class FieldRef extends ValueElement implements PropertyRef {

    public static FieldRef create(FieldRefDTO fieldRefDTO, TypeDefProvider typeDefProvider) {
        var classType = (ClassType) TypeParser.parseType(fieldRefDTO.declaringType(), typeDefProvider);
        var klass = classType.getKlass();
        var fieldId = Id.parse(fieldRefDTO.rawFieldId());
        var field = klass.findSelfField(f -> f.idEquals(fieldId));
        if(field == null)
            field = klass.findSelfStaticField(f -> f.idEquals(fieldId));
        return new FieldRef(classType, Objects.requireNonNull(field,
                () -> "Cannot find field with ID " + fieldId + " in klass " + klass.getTypeDesc()));
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
                klass.getField(f -> f.getEffectiveTemplate() == rawField);
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

    public FieldRefDTO toDTO(SerializeContext serializeContext) {
        return new FieldRefDTO(declaringType.toExpression(serializeContext), serializeContext.getStringId(rawField));
    }

    @Override
    protected String toString0() {
        return "{\"declaringType: \"" + declaringType + "\", \"rawField\": \"" + rawField.getName() + "\"}";
    }
}
