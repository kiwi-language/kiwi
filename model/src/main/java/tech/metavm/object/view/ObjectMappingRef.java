package tech.metavm.object.view;

import tech.metavm.entity.*;
import tech.metavm.object.type.ClassType;
import tech.metavm.object.type.TypeParser;
import tech.metavm.object.view.rest.dto.ObjectMappingRefDTO;

import java.util.Objects;

@EntityType("ObjectMappingRef")
public class ObjectMappingRef extends ValueElement implements Reference {

    public static ObjectMappingRef create(ObjectMappingRefDTO sourceMappingRefDTO, IEntityContext context) {
        return new ObjectMappingRef(
                (ClassType) TypeParser.parse(sourceMappingRefDTO.declaringType(), context),
                context.getObjectMapping(sourceMappingRefDTO.rawMappingId())
        );
    }

    @ChildEntity("declaringType")
    private final ClassType declaringType;
    private final ObjectMapping rawMapping;
    private transient ObjectMapping resolved;

    public ObjectMappingRef(ClassType declaringType, ObjectMapping rawMapping) {
        this.declaringType = addChild(declaringType.copy(), "declaringType");
        this.rawMapping = rawMapping;
    }

    public ObjectMapping resolve() {
        if(resolved != null)
            return resolved;
        var klass = declaringType.resolve();
        return resolved = klass.getMapping(m -> m.getEffectiveTemplate() == rawMapping);
    }

    @Override
    protected boolean equals0(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof ObjectMappingRef that)) return false;
        return Objects.equals(declaringType, that.declaringType) && Objects.equals(rawMapping, that.rawMapping);
    }

    @Override
    public int hashCode() {
        return Objects.hash(declaringType, rawMapping);
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitObjectMappingRef(this);
    }

    public ClassType getDeclaringType() {
        return declaringType;
    }

    public ObjectMapping getRawMapping() {
        return rawMapping;
    }

    @Override
    public ObjectMappingRef copy() {
        var copy = new ObjectMappingRef(declaringType, rawMapping);
        copy.resolved = resolved;
        return copy;
    }

    public ObjectMappingRefDTO toDTO() {
        try(SerializeContext serializeContext = SerializeContext.enter()) {
            return toDTO(serializeContext);
        }
    }

    public ObjectMappingRefDTO toDTO(SerializeContext serializeContext) {
        return new ObjectMappingRefDTO(declaringType.toExpression(serializeContext), serializeContext.getStringId(rawMapping));
    }
}
