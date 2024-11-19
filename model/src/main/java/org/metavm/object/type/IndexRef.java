package org.metavm.object.type;

import org.metavm.entity.ElementVisitor;
import org.metavm.entity.Reference;
import org.metavm.entity.ValueElement;
import org.metavm.flow.KlassInput;
import org.metavm.flow.KlassOutput;
import org.metavm.util.WireTypes;

import java.util.Objects;

public class IndexRef extends ValueElement implements Reference {

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
        var resolved = klass.findIndex(f -> f.getEffectiveTemplate() == rawIndex);
        if(resolved == null) {
            for (Constraint constraint : klass.getConstraints()) {
                logger.debug("Constraint {}, matches: {}", constraint, constraint instanceof Index idx && idx.getEffectiveTemplate() == rawIndex);
            }
            logger.debug("Raw index name: {}, declaring klass: {}", rawIndex.getName(), rawIndex.getDeclaringType().getTypeDesc());
            throw new NullPointerException("Cannot find index with template " + rawIndex.getClass().getSimpleName() + " in klass " + klass.getTypeDesc());
        }
        return resolved;
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

    public void write(KlassOutput output) {
        output.write(WireTypes.INDEX_REF);
        declaringType.write(output);
        output.writeEntityId(rawIndex);
    }

    public static IndexRef read(KlassInput input) {
        var classType = (ClassType) Type.readType(input);
        var rawIndex = input.getIndex(input.readId());
        return new IndexRef(classType, rawIndex);
    }

}
