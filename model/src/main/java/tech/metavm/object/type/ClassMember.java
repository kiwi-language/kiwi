package tech.metavm.object.type;

import tech.metavm.entity.EntityType;

@EntityType
public interface ClassMember {

    Klass getDeclaringType();

}
