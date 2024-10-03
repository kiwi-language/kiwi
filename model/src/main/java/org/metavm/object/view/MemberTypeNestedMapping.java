package org.metavm.object.view;

import org.metavm.api.ChildEntity;
import org.metavm.api.EntityType;
import org.metavm.entity.Entity;
import org.metavm.object.type.Type;

import java.util.Objects;

@EntityType
public class MemberTypeNestedMapping extends Entity {
    private final Type sourceType;
    private final Type targetType;
    @ChildEntity
    private final NestedMapping nestedMapping;

    public MemberTypeNestedMapping(
            Type sourceType,
            Type targetType,
            NestedMapping nestedMapping
    ) {
        this.sourceType = sourceType;
        this.targetType = targetType;
        this.nestedMapping = addChild(nestedMapping, "nestedMapping");
    }

    public Type sourceType() {
        return sourceType;
    }

    public Type targetType() {
        return targetType;
    }

    public NestedMapping nestedMapping() {
        return nestedMapping;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (MemberTypeNestedMapping) obj;
        return Objects.equals(this.sourceType, that.sourceType) &&
                Objects.equals(this.targetType, that.targetType) &&
                Objects.equals(this.nestedMapping, that.nestedMapping);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sourceType, targetType, nestedMapping);
    }

    @Override
    public String toString0() {
        return "MemberTypeNestedMapping[" +
                "sourceType=" + sourceType + ", " +
                "targetType=" + targetType + ", " +
                "nestedMapping=" + nestedMapping + ']';
    }

}
