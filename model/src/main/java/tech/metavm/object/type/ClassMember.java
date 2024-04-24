package tech.metavm.object.type;

import tech.metavm.entity.EntityType;

@EntityType("类成员")
public interface ClassMember {

    Klass getDeclaringType();

}
