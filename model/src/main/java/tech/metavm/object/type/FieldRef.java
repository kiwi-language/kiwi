package tech.metavm.object.type;

import tech.metavm.entity.*;
import tech.metavm.object.type.rest.dto.FieldRefDTO;

import java.util.Objects;

@EntityType("FieldRef")
public class FieldRef extends ValueElement implements PropertyRef {

    public static FieldRef create(FieldRefDTO fieldRefDTO, IEntityContext context) {
        return new FieldRef(
                (ClassType) TypeParser.parse(fieldRefDTO.declaringType(), context),
                context.getField(fieldRefDTO.rawFieldId())
        );
    }

    @ChildEntity("declaringType")
    private final ClassType declaringType;
    private final Field rawField;
    private transient Field resolved;

    public FieldRef(ClassType declaringType, Field rawField) {
        this.declaringType = addChild(declaringType.copy(), "declaringType");
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

    @Override
    public FieldRef copy() {
        var copy = new FieldRef(declaringType, rawField);
        copy.resolved = resolved;
        return copy;
    }

    public FieldRefDTO toDTO(SerializeContext serializeContext) {
        return new FieldRefDTO(declaringType.toExpression(serializeContext), serializeContext.getId(rawField));
    }

    @Override
    protected String toString0() {
        return "{\"declaringType: \"" + declaringType + "\", \"rawField\": \"" + rawField.getName() + "\"}";
    }
}
