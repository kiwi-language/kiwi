package org.metavm.object.type;

import org.metavm.entity.Writable;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.Reference;
import org.metavm.entity.ValueElement;
import org.metavm.flow.KlassInput;
import org.metavm.util.MvOutput;
import org.metavm.util.WireTypes;

import java.util.Objects;

public class IndexRef extends ValueElement implements Reference, Writable {

    private final ClassType declaringType;
    private final Index rawIndex;

    public IndexRef(ClassType declaringType, Index rawIndex) {
        this.declaringType = declaringType;
        this.rawIndex = rawIndex;
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

    public void write(MvOutput output) {
        output.write(WireTypes.INDEX_REF);
        declaringType.write(output);
        output.writeEntityId(rawIndex);
    }

    public static IndexRef read(KlassInput input) {
        var classType = (ClassType) Type.readType(input);
        var rawIndex = input.getIndex(input.readId());
        return new IndexRef(classType, rawIndex);
    }

    public Index getRawIndex() {
        return rawIndex;
    }

    public String getName() {
        return rawIndex.getName();
    }

    public int getFieldCount() {
        return rawIndex.getNumFields();
    }

    public ClassType getDeclaringType() {
        return declaringType;
    }
}
