package tech.metavm.object.type;

import tech.metavm.entity.Element;
import tech.metavm.entity.EntityField;
import tech.metavm.entity.EntityType;

@EntityType("类成员")
public abstract class ClassMember extends Element  {

    @EntityField("所属类型")
    protected final ClassType declaringType;

    protected ClassMember(Long tmpId, ClassType declaringType) {
        super(tmpId);
        this.declaringType = declaringType;
    }

    public ClassType getDeclaringType() {
        return declaringType;
    }
}
