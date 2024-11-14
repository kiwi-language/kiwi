package org.metavm.object.type;

import org.metavm.entity.ElementVisitor;
import org.metavm.entity.SerializeContext;
import org.metavm.entity.ValueElement;
import org.metavm.object.instance.core.Id;
import org.metavm.object.type.rest.dto.IndexRefDTO;

import java.util.Objects;

public class IndexRef extends ValueElement {

    public static IndexRef create(IndexRefDTO indexRefDTO, TypeDefProvider typeDefProvider) {
        var classType = (ClassType) TypeParser.parseType(indexRefDTO.declaringType(), typeDefProvider);
        var klass = classType.getKlass();
        var rawIndexId = Id.parse(indexRefDTO.rawIndexId());
        var rawIndex = klass.findIndex(i -> i.idEquals(rawIndexId));
        return new IndexRef(classType, Objects.requireNonNull(rawIndex,
                () -> "Cannot find field with ID " + rawIndexId + " in klass " + klass.getTypeDesc()));
    }

    private final ClassType declaringType;
    private final Index rawIndex;
    private transient Index resolved;

    public IndexRef(ClassType declaringType, Index rawIndex) {
        this.declaringType = declaringType;
        this.rawIndex = rawIndex;
    }

    public Index resolve() {
        if(resolved != null)
            return resolved;
        var klass = declaringType.resolve();
        return resolved = Objects.requireNonNull(klass.findIndex(f -> f.getEffectiveTemplate() == rawIndex));
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitIndexRef(this);
    }

    @Override
    protected boolean equals0(Object obj) {
        return obj instanceof IndexRef that && that.declaringType.equals(declaringType) && that.rawIndex == rawIndex;
    }

    @Override
    public int hashCode() {
        return Objects.hash(declaringType, rawIndex);
    }

    public IndexRefDTO toDTO(SerializeContext serializeContext) {
        return new IndexRefDTO(declaringType.toExpression(serializeContext), serializeContext.getStringId(rawIndex));
    }

}
